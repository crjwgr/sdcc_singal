var PeerManager = (function () {
  var myId = 1;
  var localId,
      config = {
        peerConnectionConfig: {
          iceServers: [

            {
                urls: [
                  "turn:36.7.68.68:3478?transport=tcp",
                ],
                credential: "test",
                username: "test"
              }
          ]
        },

        peerConnectionConstraints: {
          optional: [
            {"DtlsSrtpKeyAgreement": true}
                   
        ]
        }
      },
      peerDatabase = {},
      localStream,
      remoteVideoContainer = document.getElementById('remoteVideosContainer'),
      socket = io("https://www.sdcti.site:8888");

	 

  	  

  socket.on('message', handleMessage);
  socket.emit('resetId', {myId:1});
 
  socket.on('acceptcall', function(){
	  
	  
	  
	  
  });
  
  socket.on('receiveCall', function(){
	  Start();
	  socket.emit('acceptcall', {myId:1,callerId:2});

	  
  });
  
  socket.on('id', function(id) {
    localId = id;
  });
  
  socket.on('id2', function(id) {
	    send("init",id ,null);
	  });
  
  
  function addPeer(remoteId) {
    var peer = new Peer(config.peerConnectionConfig, config.peerConnectionConstraints);
    peer.pc.onicecandidate = function(event) {
      if (event.candidate) {
        send2('candidate', remoteId, {
          label: event.candidate.sdpMLineIndex,
          id: event.candidate.sdpMid,
          candidate: event.candidate.candidate
        });
      }
    };
    peer.pc.onaddstream = function(event) {
      attachMediaStream(peer.remoteVideoEl, event.stream);
      remoteVideoContainer.appendChild(peer.remoteVideoEl);
    };
    peer.pc.onremovestream = function(event) {
      peer.remoteVideoEl.src = '';
      remoteVideoContainer.removeChild(peer.remoteVideoEl);
    };
    peer.pc.oniceconnectionstatechange = function(event) {
      switch(
      (  event.srcElement // Chrome
      || event.target   ) // Firefox
      .iceConnectionState) {
        case 'disconnected':
        	remoteVideoContainer.removeChild(peer.remoteVideoEl);
          break;
      }
    };
    peerDatabase[remoteId] = peer;
        
    return peer;
  }
  function answer(remoteId) {
    var pc = peerDatabase[remoteId].pc;
    pc.createAnswer(
      function(sessionDescription) {
        pc.setLocalDescription(sessionDescription);
        send2('answer', remoteId, sessionDescription);
      }, 
      error
    );
  }
  
  function offer(remoteId) {
    var pc = peerDatabase[remoteId].pc;
    pc.createOffer(
      function(sessionDescription) {
        pc.setLocalDescription(sessionDescription);
        send2('offer', remoteId, sessionDescription);
        
      }, 
      error
    );
  }
  function handleMessage(message) {
    var type = message.type,
        from = message.from,
        pc = (peerDatabase[from] || addPeer(from)).pc;

    console.log('received ' + type + ' from ' + from);
  
    switch (type) {
      case 'init':
        toggleLocalStream(pc);
        offer(from);
        break;
      case 'offer':
    	
        pc.setRemoteDescription(new RTCSessionDescription(message.payload), function(){}, error);
        toggleLocalStream(pc);
        answer(from);
        break;
      case 'answer':
    	
        pc.setRemoteDescription(new RTCSessionDescription(message.payload), function(){}, error);
        break;
      case 'candidate':
        if(pc.remoteDescription) {
          pc.addIceCandidate(new RTCIceCandidate({
            sdpMLineIndex: message.payload.label,
            sdpMid: message.payload.id,
            candidate: message.payload.candidate
          }), function(){}, error);
        }
        break;
    }
  }
  function send2(type, to, payload) {
    console.log('sending ' + type + ' to ' + to);
    socket.emit('message', {
      to: 2,
      myId:1,
      type: type,
      payload: payload
    });
  }
  function toggleLocalStream(pc) {
    if(localStream) {
    	
      (!!pc.getLocalStreams().length) ? pc.removeStream(localStream) : pc.addStream(localStream);
    }
  }
  function error(err){
    console.log(err);
  }

  return {
	  //第一步呼叫对方
	  call:function call(){
		  	localStream.name = 'Guest';
		  	
	    	 
	    	 socket.emit('startclient', {
	  	      to: 2,
	  	      myId:1,
	  	      type: "init",
	  	      payload: ""
	  	    });
	    	 
	    	 
	    	 
	    },
	    
    getId: function() {
      return localId;
    },
    
    //设置本地流
    setLocalStream: function(stream) {

      // if local cam has been stopped, remove it from all outgoing streams.
      if(!stream) {
        for(id in peerDatabase) {
          pc = peerDatabase[id].pc;
          if(!!pc.getLocalStreams().length) {
            pc.removeStream(localStream);
            offer(id);
          }
        }
      }

      localStream = stream;
    }, 

    toggleLocalStream: function(remoteId) {
      peer = peerDatabase[remoteId] || addPeer(remoteId);
      toggleLocalStream(peer.pc);
    },
    
    peerInit: function(remoteId) {
      peer = peerDatabase[remoteId] || addPeer(remoteId);
      send2('init', remoteId, null);
    },
   
    peerRenegociate: function(remoteId) {
      offer(remoteId);
    },
    see:function(){
    	socket.emit('message', {
     	      to: 2,
     	      myId:1 ,
     	      type: 'init',
     	      payload: ''
     	    });
    },
    send: function(type, payload) {
      socket.emit(type, payload);
    },
    sendM: function(type, payload) {
    	
      }
  };
  
});

var Peer = function (pcConfig, pcConstraints) {
  this.pc = new RTCPeerConnection(pcConfig, pcConstraints);
  this.remoteVideoEl = document.createElement('video');
  this.remoteVideoEl.controls = true;
  this.remoteVideoEl.autoplay = true;
}