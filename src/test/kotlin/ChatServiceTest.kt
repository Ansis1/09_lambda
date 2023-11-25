import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class ChatServiceTest {

    val chatSrv: ChatService = ChatService

    @Before
    fun clearBeforeTest() {
        chatSrv.clear()
    }

    @Test
    fun createMsg() {
        assertTrue(chatSrv.createMsg(122345, "test junit4 createMsg", isTest = true) == 1)
    }

    @Test
    fun getChats() {
        chatSrv.createMsg(2345, "test junit4 getChats", isTest = true)
        assertTrue(chatSrv.getChats().isNotEmpty())
    }

    @Test
    fun getUnreadCntChats() {
        chatSrv.createMsg(1345, "test junit4 getUnreadCntChats", isTest = true)
        chatSrv.createMsg(1345, "test junit4 getUnreadCntChats rec", isTest = true, isSender = false)
        assertTrue(chatSrv.getUnreadCntChats() == 1)
    }

    @Test
    fun getLastChatMessages() {
        chatSrv.createMsg(123452, "test junit4 getLastChatMessages", isTest = true)
        assertTrue(!chatSrv.getLastChatMessages().contains("нет сообщений"))
    }

    @Test
    fun getLastNMessagesInChat() {
        chatSrv.createMsg(123456, "test junit4 getLastNMessagesInChat", isTest = true)
        chatSrv.createMsg(123456, "test test getLastNMessagesInChat rec", isSender = false, isTest = true)
        chatSrv.currChatID = chatSrv.getLastChat()?.chatId ?: -1

        assertTrue(
            !chatSrv.getLastNMessagesInChat(chatSrv.currChatID, 5)
                .contains("В чате пока нет сообщений..") && chatSrv.getUnreadCntChats() == 0
        )
    }

    @Test
    fun deleteChat() {
        chatSrv.createMsg(1234, "test junit4 deleteChat", isTest = true)
        assertTrue(chatSrv.deleteChat(1234, true) > 0)
    }


    @Test
    fun deleteMsg() {
        chatSrv.createMsg(123, "test junit4 deleteMsg", isTest = true)
        val mId = chatSrv.getLastChat()?.msgIdLast ?: -1
        val chId = chatSrv.getLastChat()?.chatId ?: -1
        assertTrue(chatSrv.deleteMsg(chId, mId, 1, isTest = true) == 1)
    }

    @Test
    fun updateMsg() {
        chatSrv.createMsg(123435, "test junit4 updateMsg", isTest = true)
        val mId = chatSrv.getLastChat()?.msgIdLast ?: -1
        val chId = chatSrv.getLastChat()?.chatId ?: -1
        assertTrue(chatSrv.updateMsg(chId, mId, "changed text after update") == 1)
    }
}