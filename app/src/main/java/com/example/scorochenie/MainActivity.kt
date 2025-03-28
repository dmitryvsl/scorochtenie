package com.example.scorochenie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Настройка BottomNavigationView
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_rating -> {
                    loadFragment(RatingFragment())
                    true
                }
                R.id.nav_exercises -> {
                    loadFragment(ExercisesFragment())
                    true
                }
                R.id.nav_materials -> {
                    loadFragment(MaterialsFragment())
                    true
                }
                else -> false
            }
        }

        // Загружаем первый фрагмент при запуске
        if (savedInstanceState == null) {
            loadFragment(RatingFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}