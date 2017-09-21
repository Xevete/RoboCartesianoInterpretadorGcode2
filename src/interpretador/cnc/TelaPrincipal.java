package interpretador.cnc;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import static java.lang.Math.round;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TelaPrincipal extends javax.swing.JFrame {
    
    private Communication com = new Communication();
    private Interpreter inter = new Interpreter();
    private String[] portNames = com.avaliablePorts();
    
    private long inicialTime = 0;
    private int baudSelected = 3;
    private int bauds[] = {1200,4800,9600,19200,57600,115200};
    private boolean start = false;                       //false para inicio do 0 e true para inicio por linha especifica
    private boolean pause = false;                       //seta a thread de plot em pause ou não
    private int actualx=0, actualy=0, actualz=0;         //posição atual do efeutador em passos de motor
    private long line;      
    private float scale;                                 //passos de motor para deslocamento, passos por mm
    private long numberOfLines;
    private RandomAccessFile arq;
    private UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
    private int inicialLine = 0;
   
    
   
    public TelaPrincipal() {
        initComponents();
        try{
            UIManager.setLookAndFeel(looks[3].getClassName());
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch(Exception e){
            e.printStackTrace();
        }                     
        for(int i = 0; i < portNames.length; i++) //retorna portas disponiveis  
            jPorta.addItem(portNames[i]);
        for(int i = 0; i < bauds.length; i++) //retorna portas disponiveis  
            jBaud.addItem(String.valueOf(bauds[i]));
        jBaud.setSelectedIndex(baudSelected);
        com.setRate(bauds[baudSelected]);
        scale = Float.valueOf(Textescala.getText());
        com.setPort(portNames[jPorta.getSelectedIndex()]);
        jPorta.requestFocus();
    }
    
    private void openArq()           
    {
        FileFilter filter = new FileNameExtensionFilter("G-Code files (.nc, .ngc)","nc","ngc");
        JFileChooser fc = new JFileChooser("C:\\Users\\"+System.getenv("USERNAME")+"\\Desktop");     
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(filter);
        int resultado = fc.showOpenDialog(this);
        if (resultado == JFileChooser.CANCEL_OPTION) return;
        jPause.setForeground(Color.blue);
        jPause.setText("Carregado");
        File nomeDoArquivo = fc.getSelectedFile();
        if (nomeDoArquivo == null || nomeDoArquivo.getName().equals(""))
            JOptionPane.showMessageDialog(this,"Nome do Arquivo inválido","Arquivo Inválido",
                    JOptionPane.ERROR_MESSAGE);
        else
            try {
                arq = new RandomAccessFile(nomeDoArquivo,"r");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,"Erro na abertura do Arquivo","Erro de Arquivo",
                        JOptionPane.ERROR_MESSAGE);
            }
        numberOfLines = getNumbOfLines();
    }
 
    private long getNumbOfLines() 
    { //define o numero de linhas a serem lidas no arquivo
        String w;
        int a=1;
        long cont = 0; //contador de linhas encontradas
        while (a == 1)
        {
            try {
                w = arq.readLine(); 
                if (w.equals(""))  //marco de fim do arquivo
                    a = 0;
                cont++;        
            }
              catch (Exception e) { 
                a = 0;
            }
        } 
        try {
            arq.seek(0);
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cont;
    }
    
    private void readLine() 
    {  //le cada linha do arquivo
        String linhatual;
            try {           
                linhatual = arq.readLine();
                if (line >= inicialLine)
                {   
                    inter.Interpret(linhatual);           
                    jTlinha.setText(String.valueOf(line));
                    if (!start) //caso inicie do 0
                        convert(inter.GetX(), inter.GetY(), inter.GetZ());
                    else //caso inicio por uma linha especifica
                    {
                        convert(inter.GetX(), inter.GetY(), 0);
                        convert(inter.GetX(), inter.GetY(), inter.GetZ());
                        start = false;
                    }               
                }
            }
              catch (Exception e) { 
                numberOfLines = 0;
                System.err.println(e);
            }       
    }

  private void convert(float a, float b, float c)  //converte as coordenadas do g-code para numero de passos dos motores
    {   //aplica translação e escala
        int x, y, z, tempx, tempy;
        x = round(a*scale);
        y = round(b*scale);
        z = round(c);  
        tempx = x;
        tempy = y; 
        x = x-actualx;
        y = y-actualy;
        actualx = tempx;
        actualy = tempy;
        actualz = z;
        send(x, y, z);
        
    }
       
    private void send(int x, int y, int z)  //envia numero de passos para a CNC
    {   
        com.sendCoordinates(x, y, z);
        updateScreen();
    }  
          
    public void updateScreen() //atualiza a tela de interface
    {
        Textxa.setText(String.format("%.2f", actualx/scale));
        Textya.setText(String.format("%.2f", actualy/scale));
        Textza.setText(String.format("%d", actualz));
        Status.setValue((int)((100.0/numberOfLines)*(line+1)));
    }

Thread t;  
public void work(){ //rotina de leitura e processamento de cada linha G-code
         t = new Thread() {
            @Override
            public void run() 
            {
                jImprimir.setEnabled(false);
                for(line=0;line<numberOfLines;line++)    //leitura individual de linhas
                    readLine();
                long milisegundos = System.currentTimeMillis();
                int segundos = (int) ((milisegundos - inicialTime )/1000);
                int minutos = segundos/60;
                segundos = segundos - minutos*60;
                milisegundos = milisegundos - ((segundos * 1000) + (minutos * 60 * 1000) + inicialTime);
                jImprimir.setEnabled(rootPaneCheckingEnabled);
                JOptionPane.showMessageDialog(null,"Fim do trabalho\nTempo gasto: "+ 
                        String.valueOf(minutos)+
                        ":"+
                        String.valueOf(segundos)+
                        ":"+
                        String.valueOf(milisegundos)+
                        " m,s,ms","Fim",
                    JOptionPane.INFORMATION_MESSAGE);
                inicialTime = 0;
                pause = false;
                jTlinha.setText("0");
                jPause.setForeground(Color.blue);
                jPause.setText("Pronto");
                try {
                      arq.seek(0);
                      } catch (IOException ex) {
                     Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                numberOfLines = 0;               
            }       
        };
        t.start();
}     

Thread e;  
public void send(final int control){ //envio manual, direcionais ou coordenada especifica
         e = new Thread() {
            @Override
            public void run() 
            {
                int x = 0, y = 0, z = 0, tempx, tempy;
                switch(control)
                {
                    case 0: x = round(Float.valueOf(Textx.getText())*scale); //manual
                            y = round(Float.valueOf(Texty.getText())*scale);
                            if (jCBstatez.isSelected())
                                z = 1;
                            else
                                z = 0;
                            break;
                    case 1: x = round(Float.valueOf(jTdeslocamento.getText()) * scale)+actualx; //left 
                            y = actualy;
                            z = actualz;
                            break;
                    case 2: x = round(-Float.valueOf(jTdeslocamento.getText()) * scale)+actualx; //rigth  
                            y = actualy;
                            z = actualz;
                            break;
                    case 3: y = round(Float.valueOf(jTdeslocamento.getText()) * scale)+actualy; //up 
                            x = actualx;
                            z = actualz;
                            break;
                    case 4: y = round(-Float.valueOf(jTdeslocamento.getText()) * scale)+actualy;  //down  
                            x = actualx;
                            z = actualz;
                            break;
                    case 5: y = actualy; //acionar ferramenta  
                            x = actualx;
                            if (actualz == 1)
                                z = 0;
                            else
                                z = 1;
                            break;
                }
                tempx = x;
                tempy = y;
                x = x-actualx;
                y = y-actualy;
                actualz = z;
                actualx = tempx;
                actualy = tempy;
                send(x,y,z);
            }       
        };
        e.start();
}     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jInicio = new javax.swing.JButton();
        jEnvia = new javax.swing.JButton();
        Textx = new javax.swing.JTextField();
        Texty = new javax.swing.JTextField();
        Textxa = new javax.swing.JTextField();
        Textya = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Textescala = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jAbrir = new javax.swing.JButton();
        jImprimir = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        Textvelocidade = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jSet = new javax.swing.JButton();
        Status = new javax.swing.JProgressBar();
        jCancelar = new javax.swing.JButton();
        jPorta = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPause = new javax.swing.JLabel();
        jTlinha = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jCBstatez = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jTdeslocamento = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        Textza = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jBaction = new javax.swing.JButton();
        jBup = new javax.swing.JButton();
        jBright = new javax.swing.JButton();
        jBleft = new javax.swing.JButton();
        jBdown = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jBaud = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CNC controle");
        setPreferredSize(new java.awt.Dimension(290, 244));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jInicio.setText("Home");
        jInicio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jInicioActionPerformed(evt);
            }
        });
        getContentPane().add(jInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 95, -1));

        jEnvia.setText("Enviar");
        jEnvia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEnviaActionPerformed(evt);
            }
        });
        getContentPane().add(jEnvia, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 200, -1, -1));

        Textx.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Textx.setText("0");
        Textx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextxActionPerformed(evt);
            }
        });
        getContentPane().add(Textx, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 30, -1));

        Texty.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Texty.setText("0");
        getContentPane().add(Texty, new org.netbeans.lib.awtextra.AbsoluteConstraints(62, 200, 30, -1));

        Textxa.setEditable(false);
        Textxa.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        getContentPane().add(Textxa, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 45, -1));

        Textya.setEditable(false);
        Textya.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        getContentPane().add(Textya, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 80, 45, -1));

        jLabel1.setText("Configurações");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 0, -1, -1));

        jLabel2.setText("X:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 82, 11, -1));

        jLabel3.setText("X: ");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 203, -1, -1));

        Textescala.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Textescala.setText("91.6");
        Textescala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextescalaActionPerformed(evt);
            }
        });
        getContentPane().add(Textescala, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 20, 30, -1));

        jLabel4.setText("Andamento");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 310, -1, -1));

        jLabel5.setText("Passos por mm:");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(143, 22, -1, -1));

        jAbrir.setText("Abrir arquivo");
        jAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAbrirActionPerformed(evt);
            }
        });
        getContentPane().add(jAbrir, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, -1, -1));

        jImprimir.setText("Executar");
        jImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jImprimirActionPerformed(evt);
            }
        });
        getContentPane().add(jImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 110, 95, -1));

        jButton2.setText("Zerar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 170, 94, -1));

        Textvelocidade.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Textvelocidade.setText("17");
        Textvelocidade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextvelocidadeActionPerformed(evt);
            }
        });
        getContentPane().add(Textvelocidade, new org.netbeans.lib.awtextra.AbsoluteConstraints(93, 20, 29, -1));

        jLabel6.setText("Tempo de passo:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 22, -1, -1));

        jSet.setText("Set");
        jSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSetActionPerformed(evt);
            }
        });
        getContentPane().add(jSet, new org.netbeans.lib.awtextra.AbsoluteConstraints(256, 18, -1, -1));
        getContentPane().add(Status, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 330, 295, -1));

        jCancelar.setText("Cancelar");
        jCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelarActionPerformed(evt);
            }
        });
        getContentPane().add(jCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 110, 95, -1));

        jPorta.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jPortaItemStateChanged(evt);
            }
        });
        jPorta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPortaActionPerformed(evt);
            }
        });
        getContentPane().add(jPorta, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, 90, -1));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 281, 10));

        jLabel7.setText("Controle manual");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 150, -1, -1));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 290, 10));

        jLabel8.setText("Y: ");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 82, -1, -1));

        jLabel9.setText("Y: ");
        jLabel9.setOpaque(true);
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(52, 203, -1, -1));

        jLabel10.setText("Z: ");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 203, -1, -1));

        jButton1.setText("Pause");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 170, 95, -1));

        jPause.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPause.setText("Status");
        getContentPane().add(jPause, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 80, 60, 20));

        jTlinha.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTlinha.setText("0");
        getContentPane().add(jTlinha, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 240, 60, -1));

        jLabel11.setText("COM:");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 52, -1, -1));

        jLabel12.setText("Coordenada:");
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, -1, -1));
        getContentPane().add(jCBstatez, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 200, -1, -1));

        jLabel13.setText("Passo:");
        getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 280, 34, -1));

        jTdeslocamento.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTdeslocamento.setText("1.0");
        getContentPane().add(jTdeslocamento, new org.netbeans.lib.awtextra.AbsoluteConstraints(123, 277, 34, -1));

        jLabel14.setText("mm");
        getContentPane().add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 280, -1, -1));

        Textza.setEditable(false);
        Textza.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        getContentPane().add(Textza, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 80, 45, -1));

        jLabel15.setText("Z: ");
        getContentPane().add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 82, -1, -1));

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jBaction.setText("O");
        jBaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBactionActionPerformed(evt);
            }
        });
        jPanel1.add(jBaction, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, -1, 30));

        jBup.setText("/\\");
            jBup.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jBupActionPerformed(evt);
                }
            });
            jPanel1.add(jBup, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, -1, 30));

            jBright.setText("<");
            jBright.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jBrightActionPerformed(evt);
                }
            });
            jPanel1.add(jBright, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, -1, 30));

            jBleft.setText(">");
            jBleft.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jBleftActionPerformed(evt);
                }
            });
            jPanel1.add(jBleft, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, -1, 30));

            jBdown.setText("\\/");
            jBdown.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jBdownActionPerformed(evt);
                }
            });
            jPanel1.add(jBdown, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, -1, 30));

            getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 200, -1, -1));

            jButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton3ActionPerformed(evt);
                }
            });
            getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(158, 287, 20, 10));

            jButton4.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton4ActionPerformed(evt);
                }
            });
            getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(158, 277, 20, 10));

            jBaud.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    jBaudItemStateChanged(evt);
                }
            });
            jBaud.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jBaudActionPerformed(evt);
                }
            });
            getContentPane().add(jBaud, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 50, 100, -1));

            jLabel16.setText("Baud rate:");
            getContentPane().add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(152, 52, -1, -1));

            pack();
        }// </editor-fold>//GEN-END:initComponents

    private void jEnviaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEnviaActionPerformed
        send(0);            
    }//GEN-LAST:event_jEnviaActionPerformed

    private void jInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jInicioActionPerformed
        actualx = 0;
        actualy = 0;
        com.home();
        updateScreen();
    }//GEN-LAST:event_jInicioActionPerformed

    private void TextescalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextescalaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextescalaActionPerformed

    private void jAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAbrirActionPerformed
        openArq();
        jTlinha.setText("0");
        Status.setValue(0);
    }//GEN-LAST:event_jAbrirActionPerformed

    private void jImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jImprimirActionPerformed
        inicialTime = System.currentTimeMillis();
        jSet.doClick();
        inicialLine = Integer.valueOf(jTlinha.getText());
        jPause.setForeground(Color.blue);
        jPause.setText("Rodando");
        Status.setValue(0);
        if (jTlinha.getText().equals("0"))
            start = false; //inicio do 0
        else
            start = true; //inicia em linha especifica
        work();
    }//GEN-LAST:event_jImprimirActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        actualx = 0;
        actualy = 0;
        updateScreen();       
    }//GEN-LAST:event_jButton2ActionPerformed

    private void TextvelocidadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextvelocidadeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextvelocidadeActionPerformed

    private void jSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSetActionPerformed
//        velocidade(Textvelocidade.getText());
        com.sendVelocityComand(Textvelocidade.getText());
        
        if (baudSelected != jBaud.getSelectedIndex())
        {
            com.sendBaudComand(bauds[jBaud.getSelectedIndex()]);  
            com.setRate(bauds[jBaud.getSelectedIndex()]);
            baudSelected = jBaud.getSelectedIndex();
        }
        scale = Float.valueOf(Textescala.getText());
    }//GEN-LAST:event_jSetActionPerformed

    private void jCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelarActionPerformed
       jImprimir.setEnabled(rootPaneCheckingEnabled);
       com.closePort();       
       try {
            arq.seek(0);
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }      
        t.stop(); 
     //  e.stop();
        pause = false;
        jPause.setForeground(Color.red);
        jPause.setText("Cancelado");
    }//GEN-LAST:event_jCancelarActionPerformed

    private void jPortaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPortaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jPortaActionPerformed

    private void jPortaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jPortaItemStateChanged
        com.setPort(portNames[jPorta.getSelectedIndex()]);
    }//GEN-LAST:event_jPortaItemStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (pause == false)
        {
            t.suspend();
            jPause.setForeground(Color.red);
            jPause.setText("Pausado");
            
        }
        else
        {
            t.resume();
            jPause.setForeground(Color.blue);
            jPause.setText("Rodando");
        }
        pause = !pause;

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jBleftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBleftActionPerformed
        send(1);  
    }//GEN-LAST:event_jBleftActionPerformed

    private void jBupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBupActionPerformed
        send(3); 
    }//GEN-LAST:event_jBupActionPerformed

    private void jBrightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrightActionPerformed
        send(2);  
    }//GEN-LAST:event_jBrightActionPerformed

    private void jBdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBdownActionPerformed
        send(4); 
    }//GEN-LAST:event_jBdownActionPerformed

    private void jBactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBactionActionPerformed
        send(5); 
    }//GEN-LAST:event_jBactionActionPerformed

    private void TextxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextxActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
       jTdeslocamento.setText(String.valueOf(Float.valueOf(jTdeslocamento.getText())+1));
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
       jTdeslocamento.setText(String.valueOf(Float.valueOf(jTdeslocamento.getText())-1));
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jBaudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBaudActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBaudActionPerformed

    private void jBaudItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jBaudItemStateChanged
//        com.SendBaudComand(baud[jBaud.getSelectedIndex()]);  
 //       com.SetRate(baud[jBaud.getSelectedIndex()]); 
       // System.out.println(baud[jBaud.getSelectedIndex()]);
    }//GEN-LAST:event_jBaudItemStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar Status;
    private javax.swing.JTextField Textescala;
    private javax.swing.JTextField Textvelocidade;
    private javax.swing.JTextField Textx;
    private javax.swing.JTextField Textxa;
    private javax.swing.JTextField Texty;
    private javax.swing.JTextField Textya;
    private javax.swing.JTextField Textza;
    private javax.swing.JButton jAbrir;
    private javax.swing.JButton jBaction;
    private javax.swing.JComboBox<String> jBaud;
    private javax.swing.JButton jBdown;
    private javax.swing.JButton jBleft;
    private javax.swing.JButton jBright;
    private javax.swing.JButton jBup;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCBstatez;
    private javax.swing.JButton jCancelar;
    private javax.swing.JButton jEnvia;
    private javax.swing.JButton jImprimir;
    private javax.swing.JButton jInicio;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jPause;
    private javax.swing.JComboBox jPorta;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton jSet;
    private javax.swing.JTextField jTdeslocamento;
    private javax.swing.JTextField jTlinha;
    // End of variables declaration//GEN-END:variables
}
