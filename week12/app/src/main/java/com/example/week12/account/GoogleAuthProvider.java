package com.example.week12.account;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

/// <summary>
/// 구글 로그인 provider — Credential Manager(구글 기본 로그인 방식)로 신원 확인 / 상태 정리를 담당
///
/// ──── 카카오/네이버와 다른 점 ────
/// 카카오/네이버는 "로그인 창 → 토큰 저장 → 프로필 조회"로 신원을 확인하지만,
/// 구글은 계정 선택창에서 그 자리에 **ID 토큰(신원이 담긴 서명된 쪽지)**을 바로 돌려준다.
/// → 별도 프로필 조회 없이, 받은 자격증명에서 이름/사진/식별자를 바로 꺼낸다.
///
/// 구글 SDK를 아는 코드는 전부 이 클래스에 모여 있고, AuthRepository는 규격으로만 본다.
/// </summary>
public class GoogleAuthProvider implements SocialAuthProvider {

    /// <summary>
    /// 구글 계정을 우리 계정 id로 만들 때 붙이는 접두사 (예: google_user@gmail.com)
    /// </summary>
    public static final String ACCOUNT_PREFIX = "google_";

    /// <summary>
    /// 웹 애플리케이션 클라이언트 ID(serverClientId) — 구글 클라우드 콘솔에서 발급.
    /// "이 로그인 요청이 어느 프로젝트 것인지" 구글에 알려주는 식별자 (비밀번호 아님).
    /// 주의: 공개 저장소에 올릴 거면 숨기는 게 안전 (학습용이라 코드에 둠)
    /// </summary>
    private static final String WEB_CLIENT_ID =
            "631825093503-67i80iemqka0pa68offjmn25tspscop7.apps.googleusercontent.com";

    /// <summary>
    /// CredentialManager 생성/상태정리에 쓰는 앱 컨텍스트
    /// (카카오/네이버 SDK와 달리 구글은 Context가 필요해 생성자로 받아둔다)
    /// </summary>
    private final Context appContext;

    /// <summary>
    /// provider 생성 (상태 정리에 쓸 앱 컨텍스트를 받아둔다)
    /// </summary>
    public GoogleAuthProvider(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /// <summary>
    /// 이 provider가 만드는 계정 id 접두사 반환
    /// </summary>
    @Override
    public String accountPrefix() {
        return ACCOUNT_PREFIX;
    }

    /// <summary>
    /// 주어진 계정 id가 구글 계정인지 (접두사로 판별)
    /// </summary>
    @Override
    public boolean owns(String accountId) {
        return accountId != null && accountId.startsWith(ACCOUNT_PREFIX);
    }

    /// <summary>
    /// 로그인 — 구글 계정 선택창을 띄우고, 고르면 그 자리에서 신원(ID 토큰)을 받아 콜백으로 전달한다.
    /// (구글 Play 서비스가 있어야 동작 — 없으면 onError로 실패 처리됨)
    /// </summary>
    @Override
    public void login(Activity activity, SocialAuthCallback callback) {
        // "Google로 로그인" 버튼용 요청 — 계정 선택/추가 전체 흐름을 보여준다
        // (바텀시트 방식 GetGoogleIdOption은 기존 계정이 없으면 바로 실패하므로, 버튼엔 이게 맞다)
        GetSignInWithGoogleOption option =
                new GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID).build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build();

        CredentialManager credentialManager = CredentialManager.create(activity);
        credentialManager.getCredentialAsync(
                activity,
                request,
                null,                           // 취소 신호 없음
                activity.getMainExecutor(),     // 결과를 메인 스레드에서 받음
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleCredential(result, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        // 계정 없음 / 사용자 취소 / Play 서비스 문제 등
                        // 사용자에겐 간단히 안내하고, 원인 추적용으로 로그에 상세를 남긴다
                        Log.e("GoogleAuth", "getCredential 실패", e);
                        callback.onFailed("구글 로그인 실패");
                    }
                });
    }

    /// <summary>
    /// 받은 자격증명이 "구글 ID 토큰"이면 거기서 신원(식별자/이름/사진)을 꺼내 콜백으로 전달
    /// </summary>
    private void handleCredential(GetCredentialResponse result, SocialAuthCallback callback) {
        Credential credential = result.getCredential();
        // 구글 ID 토큰 자격증명인지 확인 (바텀시트/버튼 두 방식의 타입을 모두 허용)
        boolean isCustom = credential instanceof CustomCredential;
        boolean isIdTokenType = isCustom
                && (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())
                || GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL.equals(credential.getType()));
        if (!isIdTokenType) {
            callback.onFailed("구글 로그인 실패");
            return;
        }
        // 자격증명 데이터(Bundle)에서 구글 ID 토큰 신원을 꺼낸다
        GoogleIdTokenCredential google = GoogleIdTokenCredential.createFrom(credential.getData());
        String id = google.getId();   // 구글 계정 식별자 (보통 이메일)
        String nickname = pickNickname(google);
        String imageUrl = google.getProfilePictureUri() != null
                ? google.getProfilePictureUri().toString() : "";
        callback.onVerified(new SocialIdentity(id, nickname, imageUrl));
    }

    /// <summary>
    /// 구글 신원에서 표시 이름을 꺼낸다 (없으면 기본값)
    /// </summary>
    private String pickNickname(GoogleIdTokenCredential google) {
        String fallback = "구글사용자";
        String name = google.getDisplayName();
        boolean hasName = name != null && !name.isEmpty();
        return hasName ? name : fallback;
    }

    /// <summary>
    /// 로그아웃 — 저장된 자격증명 상태를 지운다 (다음 로그인 때 자동 선택 없이 계정 선택창이 다시 뜸)
    /// </summary>
    @Override
    public void clearSession() {
        clearState();
    }

    /// <summary>
    /// 연동 해제 — 구글은 Credential Manager로 "동의 취소"를 클라이언트에서 직접 하는 API가 없다.
    /// 그래서 로그아웃과 같이 저장된 상태만 지운다 (다음 로그인 때 계정 선택창이 다시 뜸).
    /// </summary>
    @Override
    public void unlink() {
        clearState();
    }

    /// <summary>
    /// 저장된 자격증명 선택 상태를 비운다 (로그아웃/연동해제 공통). 결과와 무관하게 진행하므로 콜백은 비움.
    /// </summary>
    private void clearState() {
        CredentialManager credentialManager = CredentialManager.create(appContext);
        credentialManager.clearCredentialStateAsync(
                new ClearCredentialStateRequest(),
                null,
                appContext.getMainExecutor(),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void unused) {
                        // 상태 정리 성공 — 따로 할 일 없음
                    }

                    @Override
                    public void onError(ClearCredentialException e) {
                        // 실패해도 상위 로그아웃/삭제는 계속 진행
                    }
                });
    }
}
