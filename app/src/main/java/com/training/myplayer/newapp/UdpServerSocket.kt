package com.training.myplayer.newapp

import com.cuihai.framwork.utilv2.ExecUtil
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


object UdpServerSocket {
    var udpConnected = false

    var ipv4 = "10.141.34.124"
    fun start() {
        ExecUtil.execute {
            while (!udpConnected) {
                val socket = DatagramSocket(3000)
                // 2.创建数据报，用于接收客户端发送的数据
                // 2.创建数据报，用于接收客户端发送的数据
                val data = ByteArray(1024) // 创建字节数组，指定接收的数据包的大小

                val packet = DatagramPacket(data, data.size)
                // 3.接收客户端发送的数据
                // 3.接收客户端发送的数据
                println("udp-服务器端已经启动，等待客户端发送数据")
                socket.receive(packet) // 此方法在接收到数据报之前会一直阻塞

                // 4.读取数据
                // 4.读取数据
                val info = String(data, 0, packet.length)
                println("udp-我是服务器，客户端说：$info")
                ipv4 = info

                /*
                 * 向客户端响应数据
                 */
                // 1.定义客户端的地址、端口号、数据

                /*
                 * 向客户端响应数据
                 */
                // 1.定义客户端的地址、端口号、数据
//                val address: InetAddress = packet.getAddress()
//                val port: Int = packet.port
//                val data2 = "欢迎您!".toByteArray()
//                // 2.创建数据报，包含响应的数据信息
//                // 2.创建数据报，包含响应的数据信息
//                val packet2 = DatagramPacket(data2, data2.size, address, port)
//                // 3.响应客户端
//                // 3.响应客户端
//                socket.send(packet2)
//                // 4.关闭资源
//                // 4.关闭资源
//                socket.close()

                udpConnected = true
            }
        }
    }
}