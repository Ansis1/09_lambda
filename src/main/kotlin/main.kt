import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Scanner
import kotlin.system.exitProcess
import kotlin.text.StringBuilder

fun main() {
    val chatSrv = ChatService
    showGeneralInfoAndMenu(chatSrv, 1)
}

fun showGeneralInfoAndMenu(chatSrv: ChatService, step: Int) {

    println(
        "Ваши чаты: " + if (chatSrv.getUnreadCntChats() > 0) {
            " чаты с новыми сообщениями (${chatSrv.getUnreadCntChats()})"
        } else {
            ""
        }
    )
    println(chatSrv.getChats())
    println(chatSrv.getLastChatMessages())

    showMenuAndProceedChoice(chatSrv, step)

}

fun showMenuAndProceedChoice(chatSrv: ChatService, step: Int) {

    println("\n Введите код действия и нажмите Enter:")

    when (step) {

        1 -> {

            println("1. новое сообщение")
            if (chatSrv.getChatListSize() > 0) {//Есть чаты
                println("2. посмотреть N сообщений в чате.")
                println("3. удалить чат.")
            }
            println("4. обновить информацию по чатам.")
            println("6. сгенерировать сообщение от собеседника.")
        }

        2 -> {
            println("1. новое сообщение")
            println("2. отредактировать сообщение")
            println("3. удалить этот чат") // вытаскиваем из перем чат и удаляем, обновл страницу
            println("4. обновить информацию по чату.")
            println("5. вернуться в список чатов")
            println("8. удалить сообщение")
        }


    }

    println("7. завершить программу") // Всегда


    val scanner = Scanner(System.`in`)
    val input = scanner.nextLine().trim()

    if (input == "7") { //Обработка выбора
        exitProcess(0)
    } else {
        proceedChoice(input, step, chatSrv)
    }


}


fun proceedChoice(input: String, step: Int, chatSrv: ChatService) {

    val scanner = Scanner(System.`in`)
    when (input) {

        "1" -> {

            if (step != 2) {
                chatSrv.currChatID = getChatIdFromUser(scanner, input, step, chatSrv)
            }
            val txtMsg = getMessageTextFromUser(scanner)
            chatSrv.createMsg(chatSrv.currChatID, txtMsg, step = 2)

        }

        "2" -> {

            if (step != 2) {

                chatSrv.currChatID = if (chatSrv.getChatListSize() == 1) {
                    chatSrv.getLastChat()?.chatId ?: -1
                } else {
                    getChatIdFromUser(scanner, input, step, chatSrv)
                }
                println("Введите кол-во сообщений на вывод и нажмите Enter: ")
                val inputSt = scanner.nextLine().trim()
                try {

                    val inputInt = inputSt.toInt()

                    if (inputInt > 0) {
                        chatSrv.usrLastMsgCntPar = inputInt
                        println(chatSrv.getLastNMessagesInChat(chatSrv.currChatID, inputInt) + "\n")
                        showMenuAndProceedChoice(chatSrv, 2)
                    } else {
                        proceedChoice(input, step, chatSrv) //возврат на повтор.
                    }
                } catch (e: NumberFormatException) {

                    println("введено некорректное значение.")
                    proceedChoice(input, step, chatSrv) //возврат на повтор.

                }


            } else {

                val inputMsgId = getMsgIdFromUser(scanner, input, step, chatSrv)
                println("Отредактируйте текст сообщения и нажмите Enter: ")
                val inputSt = scanner.nextLine().trim()
                chatSrv.updateMsg(chatSrv.currChatID, inputMsgId, inputSt)
            }
        } // посм N сообщ в чате или change msg
        "3" -> {

            if (step != 2) {
                val chatIdd = if (chatSrv.getChatListSize() == 1) {
                    chatSrv.getLastChat()?.chatId ?: -1
                } else {
                    getChatIdFromUser(scanner, input, step, chatSrv)
                }
                chatSrv.deleteChat(chatIdd)

            } else {

                chatSrv.deleteChat(chatSrv.currChatID)

            }

        } // del chat or del this chat
        "4" -> {

            if (step == 2) {
                println(chatSrv.getLastNMessagesInChat(chatSrv.currChatID, chatSrv.usrLastMsgCntPar))
            }
            showMenuAndProceedChoice(chatSrv, step)
        } // upd main page / get last msges in chat
        "5" -> {
            chatSrv.currChatID = 0
            showGeneralInfoAndMenu(chatSrv, 1)
        }

        "6" -> {

            val chatRandom = getChatIdFromUser(scanner, input, step, chatSrv)
            chatSrv.createMsg(chatRandom, "new message " + System.currentTimeMillis(), false, 1)

        } // generate random msg from outside
        "8" -> {

            var msgId = chatSrv.getMsgIDInOnlyOneMsgChat();
            if (msgId < 0) {
                msgId = getMsgIdFromUser(scanner, input, step, chatSrv)
            }
            chatSrv.deleteMsg(chatSrv.currChatID, msgId, step)
        } // delete msg

    }
}

fun getMessageTextFromUser(scanner: Scanner): String {

    println("Введите текст сообщения и нажмите Enter (!7 для завершения программы): ")
    val inputSt = scanner.nextLine().trim()
    if (inputSt == "!7") { //Обработка выбора
        exitProcess(0)
    }
    return inputSt

}

fun getChatIdFromUser(scanner: Scanner, input: String, step: Int, chatSrv: ChatService): Long {

    println("Введите id чата и нажмите Enter (!7 для завершения программы): ")
    val inputSt = scanner.nextLine().trim()
    if (inputSt == "!7") { //Обработка выбора
        exitProcess(0)
    }
    return try {
        inputSt.toLong()
    } catch (e: NumberFormatException) {
        println("введено некорректное значение.")
        proceedChoice(input, step, chatSrv) //возврат на повтор.
        0
    }

}

fun getMsgIdFromUser(scanner: Scanner, input: String, step: Int, chatSrv: ChatService): Long {

    println("Введите id сообщения и нажмите Enter (!7 для завершения программы): ")
    val inputSt = scanner.nextLine().trim()
    if (inputSt == "!7") { //Обработка выбора
        exitProcess(0)
    }
    return try {
        inputSt.toLong()
    } catch (e: NumberFormatException) {

        println("введено некорректное значение.")
        proceedChoice(input, step, chatSrv) //возврат на повтор.
        0
    }
}


object ChatService {
    private val chatList: MutableList<DirectMsgChat> = mutableListOf()
    var currChatID: Long = 0
    var usrLastMsgCntPar: Int = 5

    fun clear() {
        chatList.clear()
        currChatID = 0
        usrLastMsgCntPar = 5
    }

    fun getChatListSize(): Int {

        return chatList.size
    }

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
            val chat = chatList.last { it.chatId.equals(chatId) }
            chat.msgIdLast++
            chat.msgIdLast
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

    fun getLastChat(): DirectMsgChat? { //Получить последний чат из списка
        return try {
            chatList.last()
        } catch (e: NoSuchElementException) {
            null
        }

    }

    fun getMsgIDInOnlyOneMsgChat(): Long { // получить ID последнего сообщения если оно единственное в чате
        return try {
            chatList.last { chat -> chat.chatId.equals(currChatID) && chat.messages.size == 1 }.msgIdLast
        } catch (e: NoSuchElementException) {
            -1
        }

    }

    //Получить список чатов
    fun getChats(): String {
        val chats: StringBuilder = java.lang.StringBuilder()
        return if (chatList.isNotEmpty()) {
            chatList.forEach {
                chats.append(
                    "Чат ${it.chatId}, обновлён: ${getHumanDate(it.updated)} " +
                            "${
                                if (it.unreadMsgCntSender > 0) {
                                    "(${it.unreadMsgCntSender})"
                                } else {
                                    ""
                                }
                            }\n"
                )
            }
            chats.toString()
        } else ""
    }

    private fun getHumanDate(timeInMs: Long): String { // перевод даты в ЧПУ-вид
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
        return formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMs), ZoneId.systemDefault()))
    }

    //Получить список последних сообщений из чатов (можно в виде списка строк). Если сообщений в чате нет (все были удалены), то пишется «нет сообщений».
    fun getLastChatMessages(): String {

        val lastMsgs: StringBuilder = StringBuilder()
        return if (chatList.isNotEmpty()) {
            lastMsgs.append("Последние сообщениям по чатам: \n")
            chatList.forEach {
                    chat: DirectMsgChat ->
                lastMsgs.append(
                    if (chat.messages.size <= 0) {
                        "Чат ${chat.chatId}: нет сообщений. \n"
                    } else {
                        "Чат ${chat.chatId} : ${chat.messages.last().text}. \n"
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
            val chat: DirectMsgChat
            try {
                chat = chatList.last { chat1 -> chat1.chatId.equals(chatId) }
                val chatMsges = chat.messages
                if (chatMsges.isNotEmpty()) {
                    lastMsgsInChat.append("Информация по последним $msgCnt сообщениям из чата $chatId: \n")
                    val relevantMsges = chatMsges.filter { msg -> msg.msgID > (chatMsges.last().msgID - msgCnt) }
                    val unreadRelCnt = relevantMsges.filter { msg -> !msg.opened }.size //непрочитанные
                    if (unreadRelCnt > 0) { //Уменьшаем счетчик непрочитанных в чате

                        val unsMsgDiff = chat.unreadMsgCntSender - unreadRelCnt
                        chatList.set(
                            chatList.indexOf(chat),
                            chat.copy(
                                unreadMsgCntSender = (if (unsMsgDiff > 0) {
                                    unsMsgDiff
                                } else {
                                    0
                                })
                            )
                        )
                    }
                    relevantMsges.forEach {
                            msg ->
                        lastMsgsInChat.append(
                            "Отправлено ${getHumanDate(msg.created)}  / ${msg.msgID} / " + if (msg.changed) {
                                " изм."
                            } else {
                                ""
                            } +
                                    "\n ${msg.text}. \n"
                        )
                        if (!msg.opened) {

                            chat.messages.set(chat.messages.indexOf(msg), msg.copy(opened = true))

                        }
                    }
                    lastMsgsInChat.toString()

                } else {
                    "В чате пока нет сообщений.."
                }

            } catch (e: NoSuchElementException) {

                "Чат не найден.."

            }


        }
    }

    //Удалить чат, т. е. целиком удалить всю переписку.
    fun deleteChat(chatId: Long, isTest: Boolean = false): Int {
        return try {
            chatList.removeAt(chatList.indexOf(chatList.last { chat: DirectMsgChat -> chat.chatId.equals(chatId) }))
            if (!isTest) showGeneralInfoAndMenu(this, 1)
            1
        } catch (e: NoSuchElementException) {
            println("Чат $chatId не найден..")
            if (!isTest) showGeneralInfoAndMenu(this, 1)
            -1
        }
    }

    //новое сообщение
    fun createMsg(chatId: Long, text: String, isSender: Boolean = true, step: Int = 1, isTest: Boolean = false): Int {
        try {
            if (chatId <= 0 || text.isEmpty()) throw IllegalArgumentException()
        } catch (e: IllegalArgumentException) {
            println("Не указаны получатель или текст сообщения.")
            return -1
        }
        val newMsg = Message(
            chatId,
            text
        ) // создаём сообщение, если чата нет, он будет автоматически создан
        val oldChat = chatList.last { chat -> chat.chatId.equals(chatId) }
        oldChat.messages.add(newMsg.copy()) // добавляем сообщение в чат.

        return if (isSender) {
            chatList.set(
                chatList.indexOf(oldChat),
                oldChat.copy(
                    msgIdLast = oldChat.msgIdLast,
                    unreadMsgCntRecipient = oldChat.unreadMsgCntRecipient + 1,
                    updated = System.currentTimeMillis()
                )

            )
            println(getLastNMessagesInChat(chatId, usrLastMsgCntPar))
            if (!isTest) showMenuAndProceedChoice(this, step)
            1
        } else { //Если сообщение от собеседника
            chatList.set(
                chatList.indexOf(oldChat),
                oldChat.copy(
                    unreadMsgCntSender = oldChat.unreadMsgCntSender + 1,
                    msgIdLast = oldChat.msgIdLast, updated = System.currentTimeMillis()
                )
            )
            if (!isTest) showGeneralInfoAndMenu(this, 1)
            1
        }
    }


    //Удалить сообщение.
    fun deleteMsg(chatId: Long, messageID: Long, step: Int, isTest: Boolean = false): Int {
        return try {
            if (chatId <= 0 || messageID <= 0) throw IllegalArgumentException()
            val chat = chatList.last { chat -> chat.chatId.equals(chatId) }
            val msgForRemove = chat.messages.last { msg -> msg.msgID.equals(messageID) }
            chat.messages.removeAt(chat.messages.indexOf(msgForRemove))
            println(getLastNMessagesInChat(chatId, usrLastMsgCntPar))
            if (!isTest) {
                showMenuAndProceedChoice(this, step)
            }
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
    fun updateMsg(chatId: Long, messageID: Long, newText: String, isTest: Boolean = false): Int {
        return try {
            if (chatId <= 0 || newText.isEmpty()) throw IllegalArgumentException()
            val chat = chatList.last { chat -> chat.chatId.equals(chatId) }
            val oldMsg = chat.messages.last { msg -> msg.msgID.equals(messageID) }
            chat.messages.set(chat.messages.indexOf(oldMsg), oldMsg.copy(changed = true, text = newText))
            println(getLastNMessagesInChat(chatId, usrLastMsgCntPar))
            1
        } catch (e: NoSuchElementException) {
            println("Сообщение или чат не найден..")
            if (!isTest) showGeneralInfoAndMenu(this, 1)
            -2
        } catch (e: IllegalArgumentException) {
            println("Не указан ID чата/ сообщение слишком короткое..")
            if (!isTest) showGeneralInfoAndMenu(this, 1)
            -1
        }
    }
}