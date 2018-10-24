package spike.chen.graphqlspike

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloCallback
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

  private var uiHandler = Handler(Looper.getMainLooper())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val orderTrackingQuery = OrderTrackingQuery.builder().build()
    val apolloClient = ApolloClient.builder()
      .serverUrl("http://batman-graphapi.nomad.eastus2.qa.jet.network/graphql")
      .okHttpClient(OkHttpClient())
      .build()

    apolloClient.query(orderTrackingQuery)
      .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
      .enqueue(ApolloCallback(object : ApolloCall.Callback<OrderTrackingQuery.Data>() {
        override fun onFailure(e: ApolloException) {
          Log.e("Main", e.message, e)
        }

        override fun onResponse(response: Response<OrderTrackingQuery.Data>) {
          mainText.text = response.data()?.user.toString()
        }
      }, uiHandler))

  }
}
