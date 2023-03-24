package br.com.joaovitorqueiroz.weatherapp.util.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import br.com.joaovitorqueiroz.weatherapp.R
import br.com.joaovitorqueiroz.weatherapp.databinding.WeatherCardViewLayoutBinding

interface IWeatherCardView {
    fun setTextMain(value: String)
    fun setTextMainDescription(value: String)
    fun setImageDrawableMain(@DrawableRes resDrawable: Int)
    fun getImageWeather(): ImageView
}

class WeatherCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), IWeatherCardView {

    private lateinit var ivMain: ImageView
    private lateinit var tvMain: TextView
    private lateinit var tvDescriptionMain: TextView
    private val binding: WeatherCardViewLayoutBinding by lazy {
        WeatherCardViewLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    }

    init {
        getFromXml(attrs, context)
    }

    private fun getFromXml(attrs: AttributeSet?, context: Context) {
        val data = context.obtainStyledAttributes(attrs, R.styleable.WeatherCardView)
        tvMain = binding.tvMain
        tvMain.text = data.getString(R.styleable.WeatherCardView_weather_text) ?: ""
        tvDescriptionMain = binding.tvMainDescription
        tvDescriptionMain.text =
            data.getString(R.styleable.WeatherCardView_description_text) ?: ""
        ivMain = binding.ivMain
        data.getDrawable(R.styleable.WeatherCardView_src_image)?.let { safeDrawable ->
            ivMain.setImageDrawable(safeDrawable)
        }
        data.recycle()
    }

    override fun setTextMain(value: String) {
        tvMain.text = value
    }

    override fun setTextMainDescription(value: String) {
        tvDescriptionMain.text = value
    }

    override fun setImageDrawableMain(@DrawableRes resDrawable: Int) {
        ivMain.setImageResource(resDrawable)
    }

    override fun getImageWeather(): ImageView = ivMain
}
