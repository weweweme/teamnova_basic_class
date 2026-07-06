package com.example.week11.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.week11.databinding.FragmentCommunityBinding;

/// <summary>
/// '커뮤니티' 탭 화면 (소셜 — 감초 역할)
/// 지금은 자리표시만. 다음 단계에서 팔로잉 피드가 이리로 옮겨오고, 랭킹은 작은 진입 버튼으로 붙는다
///
/// Unity 비유: HomeFragment와 나란히 놓인 또 하나의 UI 패널. 탭으로 둘 중 하나만 켜진다
/// </summary>
public class CommunityFragment extends Fragment {

    /// <summary>
    /// ViewBinding — fragment_community.xml의 뷰 참조
    /// </summary>
    private FragmentCommunityBinding binding;

    /// <summary>
    /// 레이아웃을 부풀려 커뮤니티 탭의 루트 뷰를 돌려준다
    /// </summary>
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /// <summary>
    /// 뷰가 사라질 때 binding 참조를 끊어 메모리 누수를 막는다
    /// </summary>
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
