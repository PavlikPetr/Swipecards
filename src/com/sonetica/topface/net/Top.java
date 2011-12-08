package com.sonetica.topface.net;

public class Top implements Packet {
  public int sex;
  public int city;
  public String ssid;
}
/*
POST http://api.topface.ru/?v=1 HTTP/1.1
User-Agent: Fiddler
Content-Type: application/json
Host: api.gamma.topface.ru
Content-Length: 99

{
"service":"top",
"data":{
"sex":0,
"city":2
},
"ssid":"c1609e5d9c8f1aef9fddcb7a39790511"
}
*/