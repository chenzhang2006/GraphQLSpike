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
import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import com.apollographql.apollo.rx.RxApollo
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import spike.chen.graphqlspike.type.CustomType

class MainActivity : AppCompatActivity() {

  private var uiHandler = Handler(Looper.getMainLooper())
  private var subscription: Subscription? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val orderTrackingQuery = OrderTrackingQuery.builder().zip(Zip("08057")).build()

    val zipCustomTypeAdapter = object : CustomTypeAdapter<Zip> {
      override fun encode(value: Zip): CustomTypeValue<*> {
        return CustomTypeValue.GraphQLString(value.zipCode)
      }

      override fun decode(value: CustomTypeValue<*>): Zip {
        return Zip(value.value.toString())
      }

    }

    val httpClient = OkHttpClient.Builder()
      .addInterceptor { chain ->
        val original = chain.request()

        // Request customization: add request headers
        val requestBuilder = original.newBuilder()
          .header("mock", "true")

        val request = requestBuilder.build()
        chain.proceed(request)
      }.build()

    val apolloClient = ApolloClient.builder()
      .serverUrl("http://batman-graphapi.nomad.eastus2.qa.jet.network/graphql")
      .okHttpClient(httpClient)
      .addCustomTypeAdapter(CustomType.ZIP, zipCustomTypeAdapter)
      .build()

    val apolloCall = apolloClient.query(orderTrackingQuery)
      .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
//      .enqueue(ApolloCallback(object : ApolloCall.Callback<OrderTrackingQuery.Data>() {
//        override fun onFailure(e: ApolloException) {
//          Log.e("Main", e.message, e)
//        }
//
//        override fun onResponse(response: Response<OrderTrackingQuery.Data>) {
//          mainText.text = response.data()?.toString()
//        }
//      }, uiHandler))

    val observable = RxApollo.from(apolloCall)
    subscription = observable.subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { response: Response<OrderTrackingQuery.Data> ->
        mainText.text = response.data()?.toString()
      }

  }

  override fun onDestroy() {
    subscription?.unsubscribe()
    super.onDestroy()
  }
}
