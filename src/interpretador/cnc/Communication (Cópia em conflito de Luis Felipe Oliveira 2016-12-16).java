package interpretador.cnc;

//import java.io.RandomAccessFile;
//import javax.swing.UIManager;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author Xevete
 */
public class Communication {
      
    private SerialPort serialPort = new SerialPort("");
    private int baudrate = 9600;
    private int charnumb = 1; // quantia de caracteres que vem da placa
    
    public String[] AvaliablePorts()
    {
        String[] portNames = SerialPortList.getPortNames(); 
        return portNames;
    }
    
    public void SetPort(String port)
    {
        serialPort = new SerialPort(port); 
    }
    
    public void SetRate(int rate)
    {
        baudrate = rate; 
    }
    
    public void SendCoordinates(int x, int y, int z)
    {
        try 
        {
            serialPort.openPort();
            serialPort.setParams(baudrate, 8, 1, 0);
            serialPort.writeString("x"+x+"y"+y+"z"+z+"\n\r");           
            serialPort.closePort();
        }
        catch (SerialPortException ex) 
        {
            System.out.println(ex);
        } 
        Waiting();
    } 
   
    private String Receiv()  //recebe os feedbacks da CNC
    {
        String buffer = "";
        try 
        {
            buffer = serialPort.readString(charnumb);      
        }
        catch (SerialPortException ex) 
        {
            System.out.println(ex);
        } 
        return buffer;
    }
   
    public void SendVelocityComand(String v)  //envia o comando que seta a velocidade dos motores, em ms de espera entre passos 10 a 4
    {
        try 
        {
            serialPort.openPort();
            serialPort.setParams(baudrate, 8, 1, 0);
            serialPort.writeString("v"+v+"\n\r");
            serialPort.closePort();
        }
        catch (SerialPortException ex) 
        {
            System.out.println(ex);
        }            
    }
    
    public void SendBaudComand(int baud)  //envia o comando que seta a velocidade de dados
    {
            int v = 0;
            switch (baud){
                case 1200:v = 1;
                          break;
                case 4800:v = 2;
                          break;
                case 9600:v = 3;
                          break;
                case 19200:v = 4;
                          break;
                case 57600:v = 5;
                          break;
                case 115200:v = 6;
                          break;                    
        }
        v = 200+v;
        try 
        {
            serialPort.openPort();
            serialPort.setParams(baudrate, 8, 1, 0);
            serialPort.writeString("v"+String.valueOf(v)+"\n\r");
            serialPort.closePort();
        }
        catch (SerialPortException ex) 
        {
            System.out.println(ex);
        }            
    }
   
    private void Waiting()  //faz o programa no pc esperar pelo feedback para enviar o proximo comando
    {
        int b = 1;         
        try 
        {
            serialPort.openPort();
            serialPort.setParams(baudrate, 8, 1, 0);        
        }
        catch (SerialPortException ex) 
        {
            System.out.println(ex);
        } 
        while (b==1)
        {
           // try { Thread.sleep (2); } catch (InterruptedException ex) {}
            if (Receiv().charAt(0) != '1')
                b = 0;              
        }
        try 
        {
           serialPort.closePort();                 
        }
        catch (SerialPortException ex) 
        {
            System.out.println(ex);
        }   
    }
   
    public void Home()  //comando fixo para chamada do HOME
    {
        try 
        {
            serialPort.openPort();
            serialPort.setParams(baudrate, 8, 1, 0);
            serialPort.writeString("+0\n\r");
            serialPort.closePort();
        }
        catch (SerialPortException ex) 
        {
            System.out.println(ex);
        }
    }
   
    public void ClosePort()  //força o fechamento da porta, usado para cancelar envio de dados
    {
        try 
        {
            serialPort.closePort();
        }
        catch (SerialPortException ex) 
        {
            System.out.println(ex);
        }  
    }  
}

