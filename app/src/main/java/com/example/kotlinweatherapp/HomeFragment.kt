package com.example.kotlinweatherapp

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.kotlinweatherapp.database.WeatherDataBase
import com.example.kotlinweatherapp.databinding.FragmentHomeBinding
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        val application = requireNotNull(this.activity).application
        val dataBase = WeatherDataBase.getInstance(application)

        val viewModel = ViewModelProvider(this, HomeViewModelFactory(dataBase!!)).get(HomeViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        fun ifErrorExistsIsTrue(){
            binding.listRecyclerView.visibility = View.GONE
        }

        fun ifErrorExistsIsFalse(){
            binding.listRecyclerView.visibility = View.VISIBLE
        }

        viewModel.globalAll.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                view?.findNavController()
                    ?.navigate(HomeFragmentDirections.actionHomeFragmentToEachWeatherFragment(it))
                Timber.d("global All has been observed")
                viewModel.doneNavigateToEachWeather()
            }
        })

        binding.listRecyclerView.adapter = RecyclerAdapter(RecyclerListener { all ->
            viewModel.setGlobalAll(all)
        })

        viewModel.doesErrorExist.observe(viewLifecycleOwner, Observer {
            if (it == true) {ifErrorExistsIsTrue()}
            else {ifErrorExistsIsFalse()}
        })

        val timer = object : CountDownTimer(1000, 500){
            override fun onFinish() {
                viewModel.getFutureProperties()
            }

            override fun onTick(millisUntilFinished: Long) {
                viewModel.getFutureProperties()
            }

        }

        timer.start()


        return binding.root
    }
}