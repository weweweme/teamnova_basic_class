package com.example.week11.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.week11.databinding.FragmentHomeBinding;

/// <summary>
/// '내 일지' 탭 화면 (개인 기록 허브)
/// 지금은 자리표시만. 다음 단계에서 기존 HomeActivity의 내용(프로필/통계/보관함/최근활동)이 이리로 옮겨옴
///
/// Unity 비유: 하나의 UI 패널(Prefab). MainActivity라는 Canvas가 탭에 따라 이 패널을 켠다
/// </summary>
public class HomeFragment extends Fragment {

    /// <summary>
    /// ViewBinding — fragment_home.xml의 뷰들을 코드에서 안전하게 참조
    /// 프래그먼트는 뷰가 Activity보다 먼저 없어질 수 있어, onDestroyView에서 binding을 비워 누수를 막는다
    /// </summary>
    private FragmentHomeBinding binding;

    /// <summary>
    /// 화면(뷰)을 만드는 단계 — 레이아웃을 부풀려(inflate) 루트 뷰를 돌려준다
    /// Unity 비유: Instantiate(prefab) 로 패널을 실제로 만들어 화면에 올리는 순간
    /// </summary>
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
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
