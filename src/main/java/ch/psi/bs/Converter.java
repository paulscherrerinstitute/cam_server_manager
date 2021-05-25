package ch.psi.bs;

import java.nio.ByteBuffer;
import ch.psi.bsread.compression.Compression;
import ch.psi.bsread.converter.MatlabByteConverter;
import ch.psi.bsread.message.ChannelConfig;
import ch.psi.bsread.message.DataHeader;
import ch.psi.bsread.message.MainHeader;
import ch.psi.bsread.message.Timestamp;

/**
 * This fix issue with null compression for bsread<4.03
 */

public class Converter extends MatlabByteConverter {
	@SuppressWarnings("unchecked")
	@Override
	public <V> V getValue(MainHeader mainHeader, DataHeader dataHeader, ChannelConfig channelConfig, ByteBuffer receivedValueBytes, Timestamp iocTimestamp) {
                if (channelConfig.getCompression() == null){
                    channelConfig.setCompression(Compression.none);
                }
                return super.getValue(mainHeader, dataHeader, channelConfig, receivedValueBytes, iocTimestamp);
	}
}
