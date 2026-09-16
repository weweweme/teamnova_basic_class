<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;

// ============================================================
// PostImageController — 글 본문에 넣을 사진 올리기 (글쓰기 화면의 JS 가 부른다)
//
//   올린 뒤 주소를 돌려주면, 화면의 JS 가 커서 자리에 ![](주소) 를 끼워 넣는다.
//   그래서 본문 어디에 사진이 있는지는 본문 자신이 들고 있고, 따로 표를 두지 않는다.
// ============================================================
class PostImageController extends Controller
{
    public function store(Request $request)
    {
        $request->validate([
            // ★ 'image' 는 확장자가 아니라 실제 파일 형식을 본다.
            //   .jpg 로 이름만 바꾼 PHP 파일은 여기서 걸린다.
            'image' => ['required', 'image', 'mimes:jpeg,png,webp,gif', 'max:4096'],
        ], [
            'image.image' => '이미지 파일만 올릴 수 있습니다.',
            'image.mimes' => 'JPG · PNG · WEBP · GIF 만 올릴 수 있습니다.',
            'image.max'   => '4MB 이하만 올릴 수 있습니다.',
        ]);

        $file = $request->file('image');

        // ★ 올린 사람이 정한 이름을 쓰지 않는다. 경로를 거슬러 올라가는 이름이나
        //   실행 가능한 이름이 섞일 수 있어서, 무작위로 새로 짓는다.
        $name = 'p' . $request->user()->id . '_' . bin2hex(random_bytes(8)) . '.' . $file->extension();

        $file->move(public_path('uploads/posts'), $name);

        return response()->json(['url' => '/uploads/posts/' . $name]);
    }
}
