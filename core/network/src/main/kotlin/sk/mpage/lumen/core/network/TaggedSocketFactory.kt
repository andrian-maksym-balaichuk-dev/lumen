package sk.mpage.lumen.core.network

import android.net.TrafficStats
import java.net.InetAddress
import java.net.Socket
import javax.net.SocketFactory

internal class TaggedSocketFactory(
    private val delegate: SocketFactory = getDefault(),
) : SocketFactory() {
    override fun createSocket(): Socket = tagged { delegate.createSocket() }

    override fun createSocket(
        host: String,
        port: Int,
    ): Socket = tagged { delegate.createSocket(host, port) }

    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int,
    ): Socket = tagged { delegate.createSocket(host, port, localHost, localPort) }

    override fun createSocket(
        host: InetAddress,
        port: Int,
    ): Socket = tagged { delegate.createSocket(host, port) }

    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): Socket = tagged { delegate.createSocket(address, port, localAddress, localPort) }

    private inline fun tagged(create: () -> Socket): Socket {
        TrafficStats.setThreadStatsTag(TAG)
        return create()
    }

    private companion object {
        const val TAG = 0x4C554D
    }
}
