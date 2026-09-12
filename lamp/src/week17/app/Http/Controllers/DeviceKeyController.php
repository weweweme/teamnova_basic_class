<?php

namespace App\Http\Controllers;

use App\Services\DeviceKey;
use App\Services\DeviceTracker;
use Illuminate\Http\Request;

// ============================================================
// DeviceKeyController — 도장 등록 · 숫자 발급 · 자국 확인
//   지금 session/enroll.php · challenge.php · verify.php 에 해당한다.
//   모두 브라우저 JS가 부르는 주소라 JSON 으로 답한다.
// ============================================================
class DeviceKeyController extends Controller
{
    // ── 도장 등록 (POST /session/key) ───────────────────────
    //   ★ 아무 때나 등록하면 안 된다. 훔친 세션으로 자기 도장을 심을 수 있기 때문이다.
    //     그래서 '비밀번호를 막 확인한 직후 3분' 안에서만 받는다.
    public function enroll(Request $request, DeviceKey $keys, DeviceTracker $devices)
    {
        if (! $keys->canEnroll($request)) {
            return response()->json(['error' => 'enroll_window_closed'], 403);
        }

        $data = $request->validate([
            'public_key' => ['required', 'string', 'max:200'],
            // ★ 공개키만 받으면 안 된다.
            //   '그 도장을 실제로 갖고 있다'는 것까지 보여야
            //   남의 공개키를 대신 등록해 두는 장난을 막을 수 있다(소유 증명).
            'signature'  => ['required', 'string', 'max:200'],
        ]);

        // 방금 발급한 숫자를 꺼내면서 지운다
        $challenge = $keys->takeChallenge($request);
        if ($challenge === null) {
            return response()->json(['error' => 'challenge_expired'], 400);
        }

        // 보낸 공개키로 그 서명이 실제로 검증되는지 확인한다
        if (! $keys->verifySignature($data['public_key'], $challenge, $data['signature'])) {
            return response()->json(['error' => 'bad_signature'], 403);
        }

        $userId   = $request->user()->id;
        $deviceId = $devices->deviceId($request);

        if (! $keys->savePublicKey($userId, $deviceId, $data['public_key'])) {
            return response()->json(['error' => 'invalid_key'], 422);
        }

        // 등록한 순간은 방금 본인 확인을 마친 상태이므로 도장을 찍은 것으로 본다
        $keys->markProved($request, $userId, $deviceId);
        $keys->closeEnrollWindow($request);

        return response()->json(['ok' => true]);
    }

    // ── 숫자 발급 (POST /session/challenge) ─────────────────
    public function challenge(Request $request, DeviceKey $keys)
    {
        return response()->json(['challenge' => $keys->issueChallenge($request)]);
    }

    // ── 자국 확인 (POST /session/verify) ────────────────────
    public function verify(Request $request, DeviceKey $keys, DeviceTracker $devices)
    {
        $data = $request->validate(['signature' => ['required', 'string', 'max:200']]);

        // ★ 숫자는 꺼내면서 지운다. 같은 숫자를 두 번 쓸 수 없다.
        $challenge = $keys->takeChallenge($request);
        if ($challenge === null) {
            return response()->json(['error' => 'challenge_expired'], 400);
        }

        $userId    = $request->user()->id;
        $deviceId  = $devices->deviceId($request);
        $publicKey = $keys->publicKeyFor($userId, $deviceId);

        if ($publicKey === null || ! $keys->verifySignature($publicKey, $challenge, $data['signature'])) {
            return response()->json(['error' => 'bad_signature'], 403);
        }

        $keys->markProved($request, $userId, $deviceId);

        return response()->json(['ok' => true, 'valid_for' => DeviceKey::proofTtl()]);
    }
}
