package main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Queue;

import serviceInterfaces.Chunk;

public class McHandler extends Thread {
		private Queue<String> msgQueue;
		private volatile int numberOfConfirmations;
		private boolean restore;
		
		public McHandler(Queue<String> msgQueue) {
			this.msgQueue = msgQueue;
			this.numberOfConfirmations = 0;
			this.restore = false;
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				if (!msgQueue.isEmpty()) {
					String[] msg = msgQueue.poll().split("\\s",5);
					boolean isValid = updateByteContents(msg);
					if (isValid && restore) {
						String filename = new String("CHUNKS" + File.separator + new String(msg[2]) + File.separator + msg[3]);
						File tmp = new File(filename);
						long tmpSize = tmp.length();
						long readLength = 64000;
						FileInputStream readStream;
						byte[] byteChunkPart;
						try {
							readStream = new FileInputStream(tmp);
							if (tmpSize < 64000)
								readLength = tmpSize;
							byteChunkPart = new byte[(int) readLength];
							int read = readStream.read(byteChunkPart, 0, (int)readLength);
							readStream.close();
							
							sendChunk( createChunkMessage(byteChunkPart,msg[2],msg[3]));
						}catch (IOException exception) {
							exception.printStackTrace();
						}
					}
					else if (isValid && !restore){}
					else Main.errorsLog.appendLog("Message not properly written. FILE ID : " + msg[2]);
				}
			}
		}

		private boolean updateByteContents(String[] msg) {
			if (msg[0].equals("STORED") && msg[1].equals("1.0")) {
				this.numberOfConfirmations += 1;
				restore = false;
			}
			else if (msg[0].equals("GETCHUNK") && msg[1].equals("1.0")) {
				restore = true && fileExists(msg);
			}
			else return false;

			return true;
		}
		
		private boolean fileExists(String[] msg){
			File file = new File("BACKUP" + File.separator + msg[2] + File.separator + msg[3]);
			return file.exists();
		}
		
		public int getNumberOfconf(){
			return this.numberOfConfirmations;
		}
	
		private void sendChunk(byte[] messageCompleted) throws IOException{
			long Initial_t, current_t;
			Initial_t = System.currentTimeMillis();
			current_t = System.currentTimeMillis();
			while( current_t < Initial_t + 500)
				current_t = System.currentTimeMillis();
			Main.mdr.send(messageCompleted);
		}
		
		private byte[] createChunkMessage(byte[] data, String fileid, String chunkNo) throws IOException{
			String chunkInformation = "CHUNK 1.0 " + fileid + " " + chunkNo + " ";
			ByteArrayOutputStream msgStream = new ByteArrayOutputStream();
			msgStream.write(chunkInformation.getBytes());
			msgStream.write((byte) 0x0d);
			msgStream.write((byte) 0x0a);
			msgStream.write((byte) 0x0d);
			msgStream.write((byte) 0x0a);
			msgStream.write(data);
			byte[] messageCompleted = msgStream.toByteArray();
			
			return messageCompleted;
		}
}
