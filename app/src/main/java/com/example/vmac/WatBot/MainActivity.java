package com.example.vmac.WatBot;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.net.URLConnection;





public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList messageArrayList;
    private EditText inputMessage;
    private ImageButton btnSend;
    private Map<String,Object> context = new HashMap<>();
    StreamPlayer streamPlayer;
    private boolean initialRequest;
    private String outputmessage;
    private String enviatextousuario;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputMessage = (EditText) findViewById(R.id.message);



        btnSend = (ImageButton) findViewById(R.id.btn_send);
        String customFont = "Montserrat-Regular.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        inputMessage.setTypeface(typeface);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();
        mAdapter = new ChatAdapter(messageArrayList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        this.initialRequest = true;
        sendMessage();

        //Watson Text-to-Speech Service on Bluemix
        final TextToSpeech service = new TextToSpeech();
        //Colocar na linha abaixo o usuário e senha do Texto to Speech, dentro das aspas
        service.setUsernameAndPassword("", "");



        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        Message audioMessage;
                        try {

                            audioMessage =(Message) messageArrayList.get(position);
                            streamPlayer = new StreamPlayer();
                            if(audioMessage != null && !audioMessage.getMessage().isEmpty())
                                //Change the Voice format and choose from the available choices
                                streamPlayer.playStream(service.synthesize(audioMessage.getMessage(), Voice.PT_ISABELA).execute());
                            else
                                streamPlayer.playStream(service.synthesize("No Text Specified", Voice.PT_ISABELA).execute());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkInternetConnection()) {


                        sendMessage();
                        enviadados();

                }
            }
        });
    };

    // Sending a message to Watson Conversation Service
    private void sendMessage() {

        final String inputmessage = this.inputMessage.getText().toString().trim();
        if(!this.initialRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            messageArrayList.add(inputMessage);
            enviatextousuario = inputmessage;

        }
        else
        {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            this.initialRequest = false;
            Toast.makeText(getApplicationContext(),"Tap on the message for Voice",Toast.LENGTH_LONG).show();

        }

        this.inputMessage.setText("");
        mAdapter.notifyDataSetChanged();

        Thread thread = new Thread(new Runnable(){




            public void run() {
                Message audioMessage;
                try {

        ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_09_20);
        //Colocar abaixo o usuário e senha do Watson Conversation, dentro das aspas
        service.setUsernameAndPassword("", "");
        MessageRequest newMessage = new MessageRequest.Builder().inputText(inputmessage).context(context).build();
        //Colocar abaixo o workspace ID do Watson Conversation, dentro das aspas
        MessageResponse response = service.message("", newMessage).execute();



        final TextToSpeech service2 = new TextToSpeech();
        //Colocar na linha abaixo o usuário e senha do Texto to Speech, dentro das aspas
        service2.setUsernameAndPassword("", "");


                    //Passing Context of last conversation
                //if(response.getContext() !=null) //ORIGINAL
                    if(response.getContext() !=null)
                    {
                        context.clear();
                        context = response.getContext();

                    }
        Message outMessage=new Message();
          if(response!=null)
          {
              if(response.getOutput()!=null && response.getOutput().containsKey("text"))
              {

                  //final String outputmessage = response.getOutput().get("text").toString().replace("[","").replace("]","");
                  outputmessage = response.getOutput().get("text").toString().replace("[","").replace("]","");
                  outMessage.setMessage(outputmessage);
                  //outMessage.setMessage("Pedro"); //funcionou e exibe somente na resposta do servidor

                  outMessage.setId("2");
                  messageArrayList.add(outMessage);



              }

              runOnUiThread(new Runnable() {
                  public void run() {
                      mAdapter.notifyDataSetChanged();
                     if (mAdapter.getItemCount() > 1) {
                          recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount()-1);

                      }

                  }
              });


          }

                    //audioMessage =(Message) messageArrayList.get(position);
                    streamPlayer = new StreamPlayer();
                    //if(audioMessage != null && !audioMessage.getMessage().isEmpty())
                    //Change the Voice format and choose from the available choices
                    streamPlayer.playStream(service2.synthesize(outputmessage, Voice.PT_ISABELA).execute());
                    //else
                    //   streamPlayer.playStream(service.synthesize("No Text Specified", Voice.PT_ISABELA).execute());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();



    }

    /**
     * Check Internet Connection
     * @return
     */
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected){
            return true;
        }
       else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    // INICIO DO ENVIA DADOS PARA O NODE-RED
    private void enviadados() {

         // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        //String url ="http://automatizaph.mybluemix.net/appget?enviadados="+enviatextousuario;
       //String url ="http://seguranca.mybluemix.net/appandroidv2g?texto="+enviatextousuario;
        String url ="http://seguranca.mybluemix.net/monitoramentoappandroidget?texto="+enviatextousuario;


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);







    }// FIM DO ENVIADADOS

}

