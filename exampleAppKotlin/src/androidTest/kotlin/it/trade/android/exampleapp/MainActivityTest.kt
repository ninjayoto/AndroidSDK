package it.trade.android.exampleapp


import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObjectNotFoundException
import android.support.test.uiautomator.UiSelector
import android.view.View
import it.trade.android.sdk.internal.TradeItKeystoreService
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule: ActivityTestRule<MainActivity> = object : ActivityTestRule<MainActivity>(MainActivity::class.java) {
        override fun beforeActivityLaunched() {
            clearSharedPrefs(InstrumentationRegistry.getTargetContext(), TradeItKeystoreService.TRADE_IT_SHARED_PREFS_KEY)
            super.beforeActivityLaunched()
        }
    }

    private fun clearSharedPrefs(context: Context, name: String) {
        val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
    }

    @Test
    @Throws(InterruptedException::class, UiObjectNotFoundException::class)
    fun oAuthFlowToTradeTest() {
        testOauthFlow("dummy")
        testGetLinkedBrokers(1)
        testAuthenticateFirstLinkedBroker()
        testPositionsFirstLinkedBrokerAccount()
        testPreviewAndPlaceTradeFirstLinkedBrokerAccount()
        testDeleteAllLinkedBrokers()
    }

    @Test
    @Throws(InterruptedException::class, UiObjectNotFoundException::class)
    fun oAuthFlowMultipleLinkedBrokerTest() {
        testOauthFlow("dummyMultiple")
        testOauthFlow("dummy")
        testGetLinkedBrokers(2)
        testAuthenticateAllLinkedBroker(2)
        testRefreshAllBalanceForAllLinkedBroker()
        testDeleteAllLinkedBrokers()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDummySecurity() {
        testOauthFlow("dummySecurity")

        tapOnText(MainActivity.MainActivityActions.AUTHENTICATE_FIRST_LINKED_BROKER.label)

        waitAwhile() //TODO there should be a better way for waiting

        checkFieldContainsText(R.id.alertTitle, "What is your mother's maiden name")

        val editText = onView(
                allOf<View>(withClassName(`is`("android.widget.EditText")),
                        withParent(allOf<View>(withId(R.id.custom),
                                withParent(withId(R.id.customPanel)))),
                        isDisplayed()))
        editText.perform(replaceText("tradingticket"), closeSoftKeyboard())

        tapOnText("OK")

        waitAwhile() //TODO there should be a better way for waiting

        checkFieldContainsText(R.id.linked_brokers_textview, "1 PARCELED LINKED BROKERS")

        navigateUp()

        testDeleteAllLinkedBrokers()
    }

    @Throws(InterruptedException::class, UiObjectNotFoundException::class)
    private fun testOauthFlow(dummyLogin: String) {
        tapOnText(MainActivity.MainActivityActions.OAUTH_LINKED_A_BROKER.label)

        waitAwhile() //TODO there should be a better way for waiting

        checkFieldContainsText(R.id.oAuthTextViewResult, "Brokers available:")

        //select dummy broker in the spinner
        selectBroker("Dummy Broker")

        tapOnText("Link broker")

        waitAwhile() //TODO there should be a better way for waiting

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val login = device.findObject(UiSelector().className("android.widget.EditText").instance(0))
        login.clearTextField()
        login.click()
        login.text = dummyLogin

        val password = device.findObject(UiSelector().className("android.widget.EditText").instance(1))
        password.clearTextField()
        password.click()
        password.text = "dummy"

        val signInButton = device.findObject(UiSelector().className("android.widget.Button").instance(0))
        signInButton.click()

        waitAwhile() //TODO there should be a better way for waiting

        if (dummyLogin in arrayOf("dummySecurity", "dummyOption")) {
            device.findObject(UiSelector().className("android.widget.EditText").instance(0)).run {
                clearTextField()
                click()
                text = if (dummyLogin == "dummySecurity") "tradingticket" else "option 1"
            }

            val submitButton = device.findObject(UiSelector().className("android.widget.Button").instance(0))
            submitButton.click()

            waitAwhile() //TODO there should be a better way for waiting
        }

        checkFieldContainsText(R.id.oAuthTextViewResult, "oAuthFlow Success:")

        navigateUp()
    }

    private fun selectBroker(brokerLongName: String) {
        val appCompatSpinner = onView(
                allOf<View>(withId(R.id.brokers_spinner),
                        withParent(allOf<View>(withId(R.id.activity_oauth_link_broker),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()))
        appCompatSpinner.perform(click())

        val appCompatCheckedTextView = onView(
                allOf<View>(withId(android.R.id.text1), withText(brokerLongName), isDisplayed()))

        appCompatCheckedTextView.perform(click())
    }

    @Throws(InterruptedException::class)
    private fun testGetLinkedBrokers(number: Int) {
        tapOnText(MainActivity.MainActivityActions.GET_LINKED_BROKERS.label)

        waitAwhile()

        checkFieldContainsText(R.id.linked_brokers_textview, number.toString() + " PARCELED LINKED BROKERS")

        navigateUp()
    }

    @Throws(InterruptedException::class)
    private fun testAuthenticateFirstLinkedBroker() {
        tapOnText(MainActivity.MainActivityActions.AUTHENTICATE_FIRST_LINKED_BROKER.label)

        waitAwhile() //TODO there should be a better way for waiting

        checkFieldContainsText(R.id.linked_brokers_textview, "1 PARCELED LINKED BROKERS")

        navigateUp()
    }

    @Throws(InterruptedException::class)
    private fun testAuthenticateAllLinkedBroker(number: Int) {
        tapOnText(MainActivity.MainActivityActions.AUTHENTICATE_ALL_LINKED_BROKERS.label)

        waitAwhile() //TODO there should be a better way for waiting

        checkFieldContainsText(R.id.linked_brokers_textview, number.toString() + " PARCELED LINKED BROKERS")

        navigateUp()
    }

    @Throws(InterruptedException::class)
    private fun testRefreshAllBalanceForAllLinkedBroker() {
        tapOnText(MainActivity.MainActivityActions.REFRESH_ALL_BALANCES_FIRST_LINKED_BROKER.label)

        waitAwhile(5000L)

        checkFieldContainsText(R.id.linked_broker_accounts_textview, "Refreshed first account balance again just to test.\n# of linkedBroker accounts: ")

        navigateUp()
    }

    @Throws(InterruptedException::class)
    private fun testPositionsFirstLinkedBrokerAccount() {
        tapOnText(MainActivity.MainActivityActions.GET_POSITIONS_FIRST_LINKED_BROKER_ACCOUNT.label)

        waitAwhile()

        checkFieldContainsText(R.id.positions_textview, "[TradeItPosition{costbasis=103.34, holdingType='LONG', lastPrice=112.34, quantity=1.0, symbol='AAPL', symbolClass='EQUITY_OR_ETF'")

        navigateUp()
    }

    @Throws(InterruptedException::class)
    private fun testPreviewAndPlaceTradeFirstLinkedBrokerAccount() {
        scrollAndTapOnText(MainActivity.MainActivityActions.PREVIEW_TRADE_FIRST_LINKED_BROKER_ACCOUNT.label)

        waitAwhile() //TODO there should be a better way for waiting

        checkFieldContainsText(R.id.preview_order_textview, "TradeItPreviewStockOrEtfOrderResponseParcelable{orderId='1', orderDetails=TradeItOrderDetailsParcelable{orderSymbol='GE', orderAction='buy', orderQuantity=1.0, orderExpiration='day', orderPrice='$20.00', orderValueLabel='Estimated Cost', orderCommissionLabel='Broker fee', orderMessage='You are about to place a limit order to buy GE', lastPrice='null', bidPrice='null', askPrice='null'")
        //place trade
        tapOnText("Place trade")

        waitAwhile() //TODO there should be a better way for waiting

        checkFieldContainsText(R.id.preview_order_textview, "TradeItPlaceStockOrEtfOrderResponseParcelable{broker='Dummy', confirmationMessage='Your order message")

        navigateUp()
    }

    private fun waitAwhile(millis: Long = 3000L) {
        Thread.sleep(millis)
    }

    private fun testDeleteAllLinkedBrokers() {
        tapOnText(MainActivity.MainActivityActions.DELETE_ALL_LINKED_BROKERS.label)

        checkFieldContainsText(android.R.id.message, "# of linkedBrokers after deletion: 0")

        tapOnText("OK")
    }

    private fun checkFieldContainsText(fieldId: Int, text: String) {
        onView(withId(fieldId)).check(matches(withText(containsString(text))))
    }

    private fun tapOnText(text: String) {
        val textView = onView(
                allOf<View>(withText(text), isDisplayed()))
        textView.perform(click())
    }

    private fun scrollAndTapOnText(text: String) {
        val textView = onView(
                allOf<View>(withText(text), isDisplayed())).perform(ViewActions.scrollTo())
        textView.perform(click())
    }

    private fun navigateUp() {
        val imageButton = onView(
                allOf<View>(withContentDescription("Navigate up"),
                        withParent(allOf<View>(withId(R.id.action_bar),
                                withParent(withId(R.id.action_bar_container)))),
                        isDisplayed()))
        imageButton.perform(click())
    }
}
