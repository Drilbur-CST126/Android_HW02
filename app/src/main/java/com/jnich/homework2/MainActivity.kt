package com.jnich.homework2

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import kotlinx.android.synthetic.main.activity_main.*
import java.security.SecureRandom
import java.util.*

class MainActivity : AppCompatActivity() {
    private var date: Date? = null
    private var username = ""
    private var password = ""
    private var gender = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        edit_dob!!.showSoftInputOnFocus = false
        edit_dob!!.onFocusChangeListener = View.OnFocusChangeListener()
        { view: View, hasFocus: Boolean ->
            if (hasFocus) {
                val calendar = Calendar.getInstance()
                calendar.time = date ?: calendar.time
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(view.context, DatePickerDialog.OnDateSetListener()
                { _: DatePicker, newYear: Int, newMonth: Int, newDay: Int ->
                    val newCalendar = Calendar.getInstance()
                    newCalendar.set(newYear, newMonth, newDay)
                    date = newCalendar.time

                    val formattedMonth = newMonth + 1
                    val output = "$formattedMonth/$newDay/$newYear"
                    edit_dob!!.setText(output)
                }, year, month, day)
                dpd.show()
            }
        }
    }

    // Salt code from tuesd4y on GitHub
    // https://gist.github.com/tuesd4y/e1584120484ac24be9f00f3968a4787d
    private fun getSalt() : ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun getAge() : Int {
        val today = Calendar.getInstance()
        val dob = Calendar.getInstance()
        dob.time = date!!
        today.add(Calendar.DAY_OF_MONTH, -dob.get(Calendar.DAY_OF_MONTH) + 1)
        today.add(Calendar.MONTH, -dob.get(Calendar.MONTH))
        today.add(Calendar.YEAR, -dob.get(Calendar.YEAR))
        return today.get(Calendar.YEAR)
    }

    fun submitOnClick(_view: View) {
        txt_error!!.visibility = View.INVISIBLE
        val argon2 = Argon2Kt()
        val hashResult = argon2.hash(
            mode = Argon2Mode.ARGON2_I,
            password = edit_password!!.text!!.toString().toByteArray(),
            salt = getSalt()
        )

        username = edit_username!!.text!!.toString()
        gender = edit_gender!!.text!!.toString()

        password = ""
        val passwordIn = edit_password!!.text!!.toString()
        val passwordRetype = edit_passwordRetype!!.text!!.toString()
        val verified = if (argon2.verify(
                mode = Argon2Mode.ARGON2_I,
                encoded = hashResult.encodedOutputAsString(),
                password = passwordRetype.toByteArray()
            )) {
            true
        } else {
            txt_error!!.text = getString(R.string.txt_passwordError)
            txt_error!!.visibility = View.VISIBLE
            false
        }

        // Give error if something is not filled out correctly
        if (date == null || username.isEmpty() || gender.isEmpty() || passwordIn.isEmpty()) {
            txt_error!!.text = getString(R.string.txt_filloutError)
            txt_error!!.visibility = View.VISIBLE
        } else if (verified) {
            if (getAge() >= 13) {
                password = hashResult.encodedOutputAsString()
            } else {
                txt_error!!.text = getString(R.string.txt_ageError)
                txt_error!!.visibility = View.VISIBLE
            }
        }
    }
}