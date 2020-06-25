package com.example.serverapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.*
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.Buffer
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val server = ServerSocket(2557,10, InetAddress.getByName("0.0.0.0"))
        val database = FirebaseDatabase.getInstance();
        val databaseReference = database.getReference();
        thread {
            run {
                val client = server.accept();
                println("Client connected: ${client.inetAddress.hostAddress}");
                thread {
                    run {
                        val reader = BufferedReader(InputStreamReader(client.getInputStream()));
                        while (true) {
                            val read = reader.readLine();
                            val line = read.toString();
                            System.out.println(line);
                            val endindexCommand = line.indexOf('*');
                            val command = line.substring(0, endindexCommand);
                            if (command.compareTo("Save Game") == 0) {
                                val endindexName = line.indexOf(',');
                                val username = line.substring(endindexCommand+1, endindexName);
                                val score = line.substring(endindexName+1, line.length);
                                databaseReference.child(username).child("").setValue(score);
                            }
                            else if(command.compareTo("Load Game")==0){
                                val endindexName = line.indexOf(',');
                                val username = line.substring(endindexCommand+1, endindexName);
                                var data = ""
                                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                                        var ok = 0

                                        for (user in dataSnapshot.children) {
                                            System.out.println(client.toString());
                                            System.out.println("id: " + dataSnapshot.child(username).toString())
                                            if(user.key == username) {
                                                data = data + user.value as String
                                                ok = 1
                                            }
                                            System.out.println(data);



                                        }
                                        if(ok == 0)
                                            data = "Err USER NOT IN DATABASE"
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {}

                                })
                                while(data.compareTo("")==0){
                                    System.out.println("Waiting")
                                }
                                val writer = PrintWriter(BufferedWriter(OutputStreamWriter(client.getOutputStream())), true)
                                writer.println(data)

                            }
                        }
                    }
                }
            }
        }
    }
}
