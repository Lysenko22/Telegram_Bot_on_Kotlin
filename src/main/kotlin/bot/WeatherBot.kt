package bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import data.remote.API_KEY
import data.remote.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Filter

private const val GIF_WAITING_URL = "https://gifer.com/ru/1LBN"
private const val BOT_ANSWER_TIMEOUT = 30
private const val BOT_TOKEN = "6982007963:AAFygXrpS9aq2ezgLBmAkq_NMhiRzmHqbfg"

class WeatherBot(private val weatherRepository: WeatherRepository) {
private lateinit var country:String
    private  var _chatId:ChatId? = null
    private  val chatId by lazy {  requireNotNull((_chatId))}

fun createBot():Bot{
return bot {
timeout = BOT_ANSWER_TIMEOUT
    token = BOT_TOKEN

    dispatch {
        setUpCommands()
        setUpCallbacks()
    }
}
}

    private fun Dispatcher.setUpCallbacks() {
callbackQuery(callbackData = "getMyLocation ") {
    bot.sendMessage(chatId = chatId, text = "Відправ мені своє геоположення")
    location {
        CoroutineScope(Dispatchers.IO).launch {
          val userCounty =  weatherRepository.getReverseCountryName(
              location.latitude.toString(),location.longitude.toString(),
                "json").countryName

            country = userCounty
            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Правильно",
                        callbackData = "yes_label"
                    )
                )
            )
            callbackQuery (callbackData = "enterManually"){
                bot.sendMessage(chatId = chatId, text = "Добре, напиши своє місто")
                message(com.github.kotlintelegrambot.extensions.filters.Filter.Text){

                    country = message.text.toString()
                    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            InlineKeyboardButton.CallbackData(
                                text = "Правильно",
                                callbackData = "yes_label"
                            )
                        )
                    )
                }
            }
            bot.sendMessage(
                chatId = chatId,
                text = "Твоэ місто - ${country}, правильно? \n Якщо не вірно, напиши своє місто ще раз",
                replyMarkup = inlineKeyboardMarkup
            )
        }
    }
    callbackQuery(callbackData = "yes_label"){
        bot.apply {
            sendAnimation(chatId = chatId, animation = TelegramFile.ByUrl(GIF_WAITING_URL))
            sendMessage(chatId=chatId, text = "Дізнаємося погоду . . . ")
            sendChatAction(chatId=chatId, action = ChatAction.TYPING)
        }
        CoroutineScope(Dispatchers.IO).launch {
           val currentWeather = weatherRepository.getCurrentWeather(
                apiKey = API_KEY,
               country,
                 "no"
            )
            bot.sendMessage(chatId= chatId,
                text = """
                            ☁ Хмарність: ${currentWeather.clouds}
                            🌡 Температура:: ${currentWeather.main.temp}
                            🙎 Відчувається як:: ${currentWeather.main.feels_like}
                            💧 Волога: ${currentWeather.main.humidity}
                            🌪 Швидкість вітру: ${currentWeather.wind.speed}
                            🧭 Тиск: ${currentWeather.main.pressure}
                                         
                """.trimIndent()
            )
            bot.sendMessage(
                chatId = chatId,
                text = "Якщо Ви хочете запросити погоду ще раз, \n використайте команду /weather"
            )
            country = ""
        }
    }
}

    }

    private fun Dispatcher.setUpCommands() {
command("start"){
    _chatId = ChatId.fromId(message.chat.id)
 bot.sendMessage(
     chatId = chatId,
     text = "Привіт! Мене звати Форест. Я допоможу тобі швидко дізнатись погоду в твоєму місті" +
             " \nДля старту введіть команду /weather"
 )
}

        command("weather"){
            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Визначити моє місто ( для мобільних приладів )",
                        callbackData = "getMyLocation"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = " Написати місто самим",
                        callbackData = "enterManually"
                )
            )
            )
            bot.sendMessage(
                chatId = chatId,
                text = "Для того, щоб я дізнався погоду, \n мені потрібно знати твоє місто.",
                replyMarkup = inlineKeyboardMarkup
            )
        }
    }
}