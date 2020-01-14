package com.andruid.magic.discodruid.ui.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.andruid.magic.discodruid.R

/**
 * A simple [Fragment] subclass.
 */
class AlbumFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance() : AlbumFragment {
            return AlbumFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false)
    }


}
