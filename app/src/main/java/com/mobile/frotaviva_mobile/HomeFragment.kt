package com.mobile.frotaviva_mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.frotaviva_mobile.databinding.FragmentHomeBinding
import com.mobile.frotaviva_mobile.MainActivity
import com.mobile.frotaviva_mobile.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener {
                fetchTruckIdAndSetInActivity(user.uid)
            }.addOnFailureListener {
            }
        }
    }

    private fun fetchTruckIdAndSetInActivity(uid: String) {
        db.collection("driver").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val truckId = document.getLong("truckId")?.toInt()

                    if (truckId != null && truckId > 0) {
                        (activity as? MainActivity)?.truckId = truckId

                        val currentItemId = (activity as? MainActivity)?.binding?.navbarInclude?.bottomNavigation?.selectedItemId
                        if (currentItemId == R.id.nav_manutencoes) {
                            (activity as? MainActivity)?.navigateToMaintenance()
                        }

                    } else {
                        (activity as? MainActivity)?.truckId = null
                    }
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}