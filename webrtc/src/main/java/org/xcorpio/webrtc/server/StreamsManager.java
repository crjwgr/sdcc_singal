package org.xcorpio.webrtc.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 对连接的管理类
 * @author Administrator
 *
 */
public class StreamsManager {

	private static List<Stream> streams = new ArrayList<Stream>();
	
	public void addStream(String id,String name,String userId){
		Iterator it = streams.iterator();
		int count = 0;
		while(it.hasNext()){
			Stream	stream = (Stream) it.next();
			if(stream.getId().equals(id)){
				count=1;
			}
		}
		
		if(count!=1){
			Stream stream = new Stream(id,name,userId);
			streams.add(stream);
		}
		else{
			count = 0;
		}
	}
	
	public void removeStream(String id){
	    for(int i=0;i<streams.size();i++){
	    	if(streams.get(i).getId().equals(id)){
	    	streams.remove(i); 
	    	}
	    }

	}
	
	public void updateStream(String id,String name,String userId){
		int index = 0;
		for(int i=0;i<streams.size();i++){
			if(streams.get(i).getId().equals(id)){
				streams.get(i).setName(name);
			}
		}
	}

	public List<Stream> getStreams() {
		return streams;
	}
	
	
	public String getOtherStream(String id) {
		int index = 0;
		while(index<streams.size()&&streams.get(index).getId().equals(id)){
			index++;
		}
		return streams.get(index).getId();
	}

	public void setStreams(List<Stream> streams) {
		this.streams = streams;
	}
	
	
}
