import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Scanner
import kotlin.text.StringBuilder

fun main() {
//TODO генерация сообщения собеседником.
//TODO фиксация непрочитанного сообщения для собеседника.

    val chatSrv = ChatService

    //Первичная информация
    println(
        "Ваши чаты: " + if (chatSrv.getUnreadCntChats() > 0) {
            " чаты с новыми сообщениями (${chatSrv.getUnreadCntChats()})"
        } else {
            ""
        }
    )
    println(chatSrv.getChats())

    //Меню
    println("Выберите необходимое действие:")

        //TODO разделить меню в зависимости от состояния (есть чаты, нет и тд)
    println("1. новое сообщение:")
    println("2. посмотреть N сообщений в чате.")
    println("3. удалить чат.")
    println("4. обновить основную информацию по чатам.")
    println("5. сгенерировать сообщение от собеседника.")
    println("6. завершить программу")


    //Когда запросили п. "посмотреть N сообщений в чате"
    println("1. Введите ID чата")
    //Далее
    println("1. Введите кол-во сообщений на вывод")
    //Открыли чат
    println("1. новое сообщение")
    println("2. отредактировать сообщение")
    println("3. удалить сообщение")
    println("4. удалить этот чат")
    println("5. вернуться в список чатов")
    println("6. завершить программу")


        //val scanner: Scanner = Scanner(System.`in`)

}


object ChatService {
    private val chatList: MutableList<DirectMsgChat> = mutableListOf()

    data class DirectMsgChat(
        val chatId: Long, //ID собеседника
        val messages: MutableList<Message> = mutableListOf(),
        val unreadMsgCntSender: Long = 0,
        val unreadMsgCntRecipient: Long = 0,
        val updated: Long,
        var msgIdLast: Long, //ID последнего сообщения, для контроля счетчика.
    )


    data class Message(
        val chatId: Long, //ID собеседника, проверяем если есть чат, то в него, если нет, то создаём.
        var text: String,
        val created: Long = System.currentTimeMillis(),
        val opened: Boolean = false,
        var changed: Boolean = false,
        val msgID: Long = getMsgId(chatId),
    )

    //Создать чат. Чат создаётся, когда пользователю отправляется первое сообщение.
    fun getMsgId(chatId: Long): Long { //Получаем ID нового сообщения++ / создаём новый чат и возвращаем ID сообщ
        return try {
            chatList.filter { chat: DirectMsgChat -> chat.chatId == chatId }.last().msgIdLast++
        } catch (e: NoSuchElementException) {

            chatList.add(
                DirectMsgChat(
                    chatId,
                    updated = System.currentTimeMillis(),
                    msgIdLast = 1,
                )
            )
            1;
        }

    }

    //info сколько чатов не прочитано
    fun getUnreadCntChats(): Int {
        return try {
            chatList.filter { chat: DirectMsgChat -> chat.unreadMsgCntSender > 0 }.size
        } catch (e: NoSuchElementException) {
            println("Все чаты прочитаны..")
            0
        }
    }

    //Получить список чатов
    fun getChats(): String {
        val chats: StringBuilder = java.lang.StringBuilder()
        return if (chatList.isNotEmpty()) {
            chatList.forEach { chats.append("Чат ${it.chatId}, обновлён: ${getHumanDate(it.updated)} \n") }
            chats.toString()
        } else "Пока ни одного чата не создано.."
    }

    private fun getHumanDate(timeInMs: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
        return formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMs), ZoneId.systemDefault()))
    }

    //Получить список последних сообщений из чатов (можно в виде списка строк). Если сообщений в чате нет (все были удалены), то пишется «нет сообщений».
    fun getLastChatMessages(): String {

        val lastMsgs: StringBuilder = StringBuilder()
        lastMsgs.append("Информация по последним сообщениям из Ваших чатов: \n")
        return if (chatList.isNotEmpty()) {
            chatList.forEach {

                    chat: DirectMsgChat ->
                lastMsgs.append(
                    if (chat.messages.size <= 0) {
                        "Чат ${chat.chatId}: нет сообщений. \n"
                    } else {
                        "Чат ${chat.chatId}: ${chat.messages.last().text}. \n"
                    }
                )
            }
            lastMsgs.toString()
        } else {
            "Пока нет созданных чатов.."
        }

    }

    //Получить список сообщений из чата, указав: ID собеседника; количество сообщений.
    // После того как вызвана эта функция, все отданные сообщения автоматически считаются прочитанными.
    fun getLastNMessagesInChat(chatId: Long, msgCnt: Int): String {

        return if (chatId <= 0 || msgCnt <= 0) {
            "Некорректно указаны параметры."
        } else {
            val lastMsgsInChat: StringBuilder = StringBuilder()
            lastMsgsInChat.append("Информация по последним $Int сообщениям из чата $chatId: \n")
            val chat = chatList.filter { chat -> chat.chatId == chatId }.last()
            val chatMsges = chat.messages
            if (chatMsges.isNotEmpty()) {
                val relevantMsges = chatMsges.filter { msg -> msg.msgID > (chatMsges.last().chatId - msgCnt) }
                val unreadRelCnt = relevantMsges.filter { msg -> !msg.opened }.size //непрочитанные
                if (unreadRelCnt > 0) { //Уменьшаем счетчик непрочитанных в чате
                    chatList.set(
                        chatList.indexOf(chat),
                        chat.copy(unreadMsgCntSender = (chat.unreadMsgCntSender - unreadRelCnt))
                    )
                }
                relevantMsges.forEach {

                        msg ->
                    lastMsgsInChat.append("Отправлено ${getHumanDate(msg.created)}: ${msg.text}. \n")
                    if (!msg.opened) {

                        chat.messages.set(chat.messages.indexOf(msg), msg.copy(opened = true))

                    }
                }
                lastMsgsInChat.toString()

            } else {
                "В чате пока нет сообщений.."
            }

        }
    }

    //Удалить чат, т. е. целиком удалить всю переписку.
    fun deleteChat(chatId: Long): Int {
        return try {
            chatList.removeAt(chatList.indexOf(chatList.last { chat: DirectMsgChat -> chat.chatId == chatId }))
            1
        } catch (e: NoSuchElementException) {
            println("Чат $chatId не найден..")
            -1
        }
    }

    //новое сообщение
    fun createMsg(chatId: Long, text: String, isSender: Boolean = true) {
        try {
            if (chatId <= 0 || text.isEmpty()) throw IllegalArgumentException()
        } catch (e: IllegalArgumentException) {
            println("Не указаны получатель или текст сообщения.")
            return
        }
        val newMsg = Message(
            chatId,
            text
        ) // создаём сообщение, если чата нет, он будет автоматически создан
        val oldchat = chatList.filter { chat -> chat.chatId == chatId }
            .last()
        oldchat.messages.add(newMsg.copy()) // добавляем сообщение в чат.

        if (isSender) {
            chatList.set(
                chatList.indexOf(oldchat),
                oldchat.copy(unreadMsgCntRecipient = oldchat.unreadMsgCntRecipient + 1)
            )
        }else{ //Если сообщение от собеседника
            chatList.set(
                chatList.indexOf(oldchat),
                oldchat.copy(unreadMsgCntSender = oldchat.unreadMsgCntSender + 1)
            )

        }
    }


    //Удалить сообщение.
    fun deleteMsg(chatId: Long, messageID: Long): Int {
        return try {
            if (chatId <= 0 || messageID <= 0) throw IllegalArgumentException()
            val chat = chatList.filter { chat -> chat.chatId == chatId }.last()
            val msgForRemove = chat.messages.filter { msg -> msg.msgID == messageID }.last()
            chat.messages.removeAt(chat.messages.indexOf(msgForRemove))
            1
        } catch (e: NoSuchElementException) {
            println("Сообщение или чат не найден..")
            -2
        } catch (e: IllegalArgumentException) {
            println("Не указан ID чата/сообщения..")
            -1
        }
    }

    //Отредактировать сообщение
    fun updateMsg(chatId: Long, messageID: Long, newText: String): Int {
        return try {
            if (chatId <= 0 || newText.isEmpty()) throw IllegalArgumentException()
            val chat = chatList.filter { chat -> chat.chatId == chatId }.last()
            val oldMsg = chat.messages.filter { msg -> msg.msgID == messageID }.last()
            chat.messages.set(chat.messages.indexOf(oldMsg), oldMsg.copy(changed = true, text = newText))
            1
        } catch (e: NoSuchElementException) {
            println("Сообщение или чат не найден..")
            -2
        } catch (e: IllegalArgumentException) {
            println("Не указан ID чата/ сообщение слишком короткое..")
            -1
        }
    }
}


