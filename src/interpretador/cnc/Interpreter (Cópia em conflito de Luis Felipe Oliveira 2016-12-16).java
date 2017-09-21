package interpretador.cnc;

import java.awt.Color;
import java.io.File;
import java.io.RandomAccessFile;
import static java.lang.Math.round;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Xevete
 */
public class Interpreter {
    
  //   private RandomAccessFile arq;
     private final String caracters="0987654321,.-";
     private float x = 0, y= 0, z = 0;
     private float scalex = 1, scaley = 1, scalez = 1;
     
     public void Interpret(String CommandLine)
     {
        float tempx = x, tempy = y, tempz = z;
        String lido = CommandLine;
        
        if (lido.substring(0, 1).equals("N")) //inicia por N, linha valida
            {              
                if (lido.indexOf("Z") >= 0) //Encontra o valor de Z
                {
                    String vz = "";
                    int x = 0, localz = 0;
                    char[] clido = lido.toCharArray();
                    for (x=1;x<lido.length();x++) //define posição inicial onde comçar a ler valor do z
                    {
                        if(clido[x] == 'Z')
                        {
                            localz = x;
                            x = lido.length();
                        }
                    }       
                    for (x=localz+1;x<lido.length();x++)
                    {
                        if(caracters.indexOf(String.valueOf(clido[x])) >= 0)
                            vz = vz + String.valueOf(clido[x]);  
                        else
                            x = lido.length();
                    }
                    tempz = Float.valueOf(vz);                   
                }
                 if (lido.indexOf("X") >= 0) //Encontra o valor de X
                {
                    String vx = "";
                    int x = 0, localx = 0;
                    char[] clido = lido.toCharArray();
                    for (x=1;x<lido.length();x++)
                    {
                        if(clido[x] == 'X')
                        {
                            localx = x;
                            x = lido.length();
                        }
                    }       
                    for (x=localx+1;x<lido.length();x++)
                    {
                        if(caracters.indexOf(String.valueOf(clido[x])) >= 0)
                            vx = vx + String.valueOf(clido[x]);                                
                        else
                            x = lido.length();
                    }
                    tempx = Float.valueOf(vx);
                }
                if (lido.indexOf("Y") >= 0) //Encontra o valor de Y
                {
                    String vy = "";
                    int x = 0, localy = 0;
                    char[] clido = lido.toCharArray();
                    for (x=1;x<lido.length();x++)
                    {
                        if(clido[x] == 'Y')
                        {
                            localy = x;
                            x = lido.length();
                        }
                    }       
                    for (x=localy+1;x<lido.length();x++)
                    {
                        if(caracters.indexOf(String.valueOf(clido[x])) >= 0)
                            vy = vy + String.valueOf(clido[x]);                                
                        else
                            x = lido.length();
                    }
                    tempy = Float.valueOf(vy);
                }          

                if (tempy < 0)
                    tempy = tempy *(-1);
                if (tempx < 0)
                    tempx = tempx *(-1);
                x = tempx;
                y = tempy;
                z = tempz;
            }
         
     }
     
  /*   public  int[] des(int AtualX, int AtualY, int Atuaz)
     {
        int[] coordenadas = new int[3];
                 
        int localx, localy, localz, tempx, tempy;
        localx = round(x*scalex);
        localy = round(y*scaley);
    //    z = round(c);  
        tempx = localx;
        tempy = localy; 
   //     x = x-px;
    //    y = y-py;
    //    px = tempx;
     //   py = tempy;
        
        return coordenadas;      
     }
   */  
 /*    public void SetScaleX(float ScaleX)
     {
         scalex = ScaleX;
     }
     
     public void SetScaleY(float ScaleY)
     {
         scalex = ScaleY;
     }
     
     public void SetScaleZ(float ScaleZ)
     {
         scalex = ScaleZ;
     }*/
     public float GetX() 
     {
         return x;
     }
     public float GetY()
     {
         return y;   
     }
     public float GetZ()
     {
           if (z < 0 )
              return 1;
           else
              return 0;           
     //    return z;
     }
    
}
