/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg_expediciones;

//import com.mysql.cj.util.StringUtils;
//import java.awt.Color;
import java.awt.Toolkit; //para lanzar un beep
import java.awt.event.ItemEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/*imports para el CMD*/
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.JDialog;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

//import javax.swing.JLabel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;


//import para jtable
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;

//imports para gestión de fechas
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;





//import java.util.List;
/**
 *
 * @author mmarco
 */
public class Entregas extends javax.swing.JFrame {
    Connection con;
    Statement stmt;
    ResultSet rs;
    int curRow; //para guardar el identificador de la línea
    /**
     * Creates new form Entregas
     */
    private JDialog ventanaSecundaria;
    
    //jtable
    private DefaultTableModel model;//Modelo para tabla
    private int sel;//Variable para obtener fila seleccionada de tabla.
       
    
    public Entregas() {
        initComponents();

        //this.getContentPane().setBackground(Color.getHSBColor(141,191,72));
        //pinto el jframe color "denox"
        //this.getContentPane().setBackground(new Color(141,191,72));
        DoConnect();
        ventanas();
        
             

        // cuando cambia el estado del combobox pinto la referencia
        jCombo_ref.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    
                    String a;
                    a = jCombo_ref.getSelectedItem().toString();
                    //System.out.print(a);
                    //determino la posición del espacio para añadir la ref y el cod cliente a los campos de consulta                     
                    int pos = pos_espacio(a);
                    int pos2 = a.indexOf(' ', pos + 1);
                    System.out.println("pos1: " + pos);
                    System.out.println("pos2: " + pos2);
                    jText_referencia.setText(a.substring(0, pos));
                    if (pos2 > 0) { //si existe 2º espacio en blanco
                        jText_ref_cliente.setText(a.substring(pos + 1, pos2));
                    } else {
                        jText_ref_cliente.setText(a.substring(pos + 1, a.length()));
                    }

                    //pinto los datos en los campos correspondientes que he seleccionado con el combobox
                    //JOptionPane.showMessageDialog(null, "uahahahahah");
                    consultar_referencia();

                }
            }
        });
         //cuando cambio de estado para el combobx de pedidos proveedor
         jCombo_pedpro.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String a;
                    a = jCombo_pedpro.getSelectedItem().toString();
                    //System.out.print(a);
                    //determino la posición del espacio para añadir el nº pedido y el cod proveedor a los campos de consulta                     
                    int pos = pos_espacio(a);
                                      
                    jText_pedproNumped.setText(a.substring(0, pos));
                    jText_pedproProv.setText(a.substring(pos + 1, a.length()));
                    
                    //pinto los datos en los campos correspondientes que he seleccionado con el combobox
                    //JOptionPane.showMessageDialog(null, "uahahahahah");
                    consultar_pedpro();

                }
            }
        });
        
         jComboBox_AlbNormal.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                //SI NO HAY UN VALOR EN LA PROPIEDAD "MODEL" DEL COMBO ENTRA AQUÍ EN CUANTO SE RELLENA EL PRIMER REGISTRO Y PRODUCE UN EFECTO QUE NO QUIERO
                if (e.getStateChange() == ItemEvent.SELECTED ){
                    
                    String a;
                    a = jComboBox_AlbNormal.getSelectedItem().toString();
                   
                    //determino la posición del espacio para añadir el nº pedido y el cod proveedor a los campos de consulta                     
                    int pos = pos_espacio(a);
                    int pos2 = a.indexOf(' ', pos + 1);
                                      
                    jTextField_AlbNumAlb.setText(a.substring(0, pos));
                    
                    
                    if (pos2 > 0)
                        
                        jTextField_AlbCte.setText(a.substring(pos + 1, a.length()));
                    
                    //pinto los datos en los campos correspondientes que he seleccionado con el combobox
                    //JOptionPane.showMessageDialog(null, "uahahahahah");
                    
                     consultar_albaran_normal();
                     
                     
                    

                }
            }
        });
         
         jComboBox_OF.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("entrando listener of");
                    String a;
                    a = jComboBox_OF.getSelectedItem().toString();
                    System.out.println(a);
                    //determino la posición del espacio para hacer el substring                    
                    //int pos = pos_espacio(a);
                                      
                    jTextField_OF.setText(a.substring(0, a.length()));
                    //jText_pedproProv.setText(a.substring(pos + 1, a.length()));
                    
                    //pinto los datos en los campos correspondientes que he seleccionado con el combobox
                    //JOptionPane.showMessageDialog(null, "uahahahahah");
                    //System.out.println("borro combo padre");
                
                    consultar_of();
                    check_radiobuttons_OF();
                    check_radiobutton_tipo();
                    

                }
            }
        });
         
         jComboBox_Padres.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("entrando listener padres");
                    String a;
                    a = jComboBox_Padres.getSelectedItem().toString();
                    System.out.println(a);
                    //determino la posición del espacio para hacer el substring                    
                    //int pos = pos_espacio(a);
                                      
                    jTextField_PadreArti.setText(a.substring(0, a.length()));
                    //jText_pedproProv.setText(a.substring(pos + 1, a.length()));
                    
                    //pinto los datos en los campos correspondientes que he seleccionado con el combobox
                    //JOptionPane.showMessageDialog(null, "uahahahahah");
                    
                    consultar_padre(0);

                }
            }
        });
         
         jComboBox_Ubi.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("entrando listener ubicaciones");
                    String a;
                    a = jComboBox_Ubi.getSelectedItem().toString();
                    System.out.println(a);
                    //determino la posición del espacio para hacer el substring                    
                    //int pos = pos_espacio(a);
                                      
                    //jText_UbiDesde.setText(a.substring(0, a.length()));
                    //jText_pedproProv.setText(a.substring(pos + 1, a.length()));
                    
                    //pinto los datos en los campos correspondientes que he seleccionado con el combobox
                    //JOptionPane.showMessageDialog(null, "uahahahahah");
                    
                    //consultar_tipoUbi();

                }
            }
        });
         
         jCombo_EtqSimple_ref.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("entrando listener EtqSimple");
                    String a;
                    a = jCombo_EtqSimple_ref.getSelectedItem().toString();
                    System.out.println(a);
                    //determino la posición del espacio para hacer el substring                    
                    int pos = pos_espacio(a);
                                      
                    //jText_UbiDesde.setText(a.substring(0, a.length()));
                    jText_EtqSimple_ref.setText(a.substring(0, pos));
                    jTextField_EtqSimple_lan.setText(a.substring(pos + 1, a.length()));
                    consultar_EtqSimple_ref();
                    zpl(8,Integer.parseInt(jFormattedTextField_CantRef.getText()),0,"");
                    

                }
            }
        });
         
         jCombo_ElCorte.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("entrando listener ElCorte");
                    String a;
                    a = jCombo_ElCorte.getSelectedItem().toString();
                    System.out.println(a);
                    //determino la posición del espacio para hacer el substring                    
                    int pos = pos_espacio(a);
                                      
                    //jText_UbiDesde.setText(a.substring(0, a.length()));
                    jText_ElCorte_Alb.setText(a.substring(0, pos));
                    jTextField_ElCorte_Art.setText(a.substring(pos + 1, a.length()));
                    //jTextField_ElCorte_Color.setText(a.substring(pos + 1, a.length()));
                    consultar_ElCorte();
                    zpl(9,Integer.parseInt(jFormattedTextField_CantElCorte.getText()),0,"");
                    

                }
                
            }
        });
         
           jComboBox_Stock.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("entrando listener Stock");
                    String a;
                    a = jComboBox_Stock.getSelectedItem().toString();
                    System.out.println(a);
                    //determino la posición del espacio para hacer el substring                    
                    int pos = pos_espacio(a);
                                      
                    //jText_UbiDesde.setText(a.substring(0, a.length()));
                    jTextStock_arti.setText(a.substring(0, pos));
                    jTextStock_lote.setText(a.substring(pos + 1, a.length()));
                    //jTextField_ElCorte_Color.setText(a.substring(pos + 1, a.length()));
                    //consultar_StockLotes();
                    //zpl(9,Integer.parseInt(jFormattedTextField_CantElCorte.getText()),0,"");
                    

                }
                
            }
        });
        
        jComboBox_AlbSSCC.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                //SI NO HAY UN VALOR EN LA PROPIEDAD "MODEL" DEL COMBO ENTRA AQUÍ EN CUANTO SE RELLENA EL PRIMER REGISTRO Y PRODUCE UN EFECTO QUE NO QUIERO
                if (e.getStateChange() == ItemEvent.SELECTED ){
                    
                    String a;
                    a = jComboBox_AlbSSCC.getSelectedItem().toString();
                   
                    //determino la posición del espacio para añadir el nº pedido y el cod proveedor a los campos de consulta                     
                    int pos = pos_espacio(a);
                    int pos2 = a.indexOf(' ', pos + 1);
                                      
                    jTextField_AlbNumAlb1.setText(a.substring(0, pos));
                    
                    
                    if (pos2 > 0)
                        
                        jTextField_AlbCte1.setText(a.substring(pos + 1, a.length()));
                    
                    //pinto los datos en los campos correspondientes que he seleccionado con el combobox
                    //JOptionPane.showMessageDialog(null, "uahahahahah");
                    
                     consultar_albaran_sscc();
                     
                     
                    

                }
            }
        });
           //elimino el combo de dascher porque creo que no es necesario, lo dejo comentado
//           jCombo_alb_dasch.addItemListener(new ItemListener() {
//
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                if (e.getStateChange() == ItemEvent.SELECTED) {
//                    System.out.println("entrando listener alb dascher");
//                    String a;
//                    a = jCombo_alb_dasch.getSelectedItem().toString();
//                    System.out.println(a);
//                    //determino la posición del espacio para hacer el substring                    
//                    int pos = pos_espacio(a);
//                                      
//                    //jText_UbiDesde.setText(a.substring(0, a.length()));
//                    //jTextStock_arti.setText(a.substring(0, pos));
//                    jText_alb_dascher.setText(a.substring(pos + 1, a.length()));
//                    //jTextField_ElCorte_Color.setText(a.substring(pos + 1, a.length()));
//                    //consultar_StockLotes();
//                    //zpl(9,Integer.parseInt(jFormattedTextField_CantElCorte.getText()),0,"");
//                    
//
//                }
//                
//            }
//        });
           

         
//      
         //manejo del foco para la ventana jdialog1
         jDialog1.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog1.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog1.setVisible(false);
			}
		});
         
         jDialog3.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog3.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog3.setVisible(false);
			}
		});
          jDialog4.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog4.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog4.setVisible(false);
			}
		});
          jDialog5.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog5.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog5.setVisible(false);
			}
		});
          jDialog6.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog6.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog6.setVisible(false);
			}
		});
          jDialog7.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog7.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog7.setVisible(false);
			}
		});
          jDialog8.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog8.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog8.setVisible(false);
			}
		});
          jDialog10.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog10.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog10.setVisible(false);
			}
		});
          jDialog11.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
                                jDialog11.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				jDialog11.setVisible(false);
			}
		});
          
        
    }
    
    public void ventanas(){
      //ventanaSecundaria = new JDialog(this,"Ventana secundaria");
        /*ventanaSecundaria = new JDialog();
        	
        JLabel etiqueta = new JLabel("Hola");
	ventanaSecundaria.getContentPane().add(etiqueta);
	ventanaSecundaria.pack();
        ventanaSecundaria.setLocationRelativeTo(this);
        
         ventanaSecundaria.addWindowListener(new WindowAdapter() {
                        @Override
			public void windowClosing(WindowEvent e) {
				//JFrame.setVisible(true);
				ventanaSecundaria.setVisible(false);
			}
		
                        @Override
			public void windowClosed(WindowEvent e) {
				//ventanaPrincipal.setVisible(true);
				ventanaSecundaria.setVisible(false);
			}
		}); */
        //centro el jdialog al jframe 
        jDialog1.setLocationRelativeTo(this);//hago que el dialog se abra centrado relativo al jframe activo
        jDialog2.setLocationRelativeTo(this);
        jDialog3.setLocationRelativeTo(this);
        jDialog4.setLocationRelativeTo(this);
        jDialog5.setLocationRelativeTo(this);
        jDialog6.setLocationRelativeTo(this);
        jDialog7.setLocationRelativeTo(this);
        jDialog8.setLocationRelativeTo(this);
        jDialog9.setLocationRelativeTo(this);
        jDialog10.setSize(1900, 600);
        jDialog10.setLocationRelativeTo(this);
        jDialog11.setLocationRelativeTo(this);
        
                
        //pongo un título 
        jDialog1.setTitle("Impresión de referencias");
        //pinto pero no va
        //JOptionPane.showMessageDialog(this, "pinto background del dialogo");
        //jDialog1.setBackground(Color.getHSBColor(141,191,72)); //no va ??
    }
    public void DoConnect() {
        /*es necesario meter en try catch las operaciones con conexión a db*/
        try {
            System.out.println("Conectando MySQL...");
            String host = "jdbc:mysql://192.168.35.25:3306/mariodb?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC";
            String uName = "famesa";
            String uPass = "@Famesa123";
            con = DriverManager.getConnection(host, uName, uPass);

            //Statement stmt = con.createStatement();//esta conexión sólo permite navegar con los registros hacia adelante
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
           
        } catch (SQLException err) {
            System.out.println(err.getMessage());
        }

    }
    
    public void DoConnect_MSSQL() {
        /*es necesario meter en try catch las operaciones con conexión a db*/
        try {
            //String host = "jdbc:mysql://192.168.35.25:3306/mariodb?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC";
            System.out.println("Conectando MSSQL...");
            //String host = "jdbc:sqlserver://213.149.235.131:49412;databaseName=x3famesa;schema=FAMESAOF";
            //String host = "jdbc:sqlserver://192.168.50.10:49412;databaseName=x3famesa;schema=FAMESAOF";
            String host = "jdbc:sqlserver://192.168.28.100:51439;databaseName=x3;schema=LIVE";
            String uName = "it";
            String uPass = "@Famesa123";
            con = DriverManager.getConnection(host, uName, uPass);

            //Statement stmt = con.createStatement();//esta conexión sólo permite navegar con los registros hacia adelante
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
           
        } catch (SQLException err) {
            System.out.println(err.getMessage());
        }
    }
    
    public void DoConnect_MSSQLV12() {
        /*es necesario meter en try catch las operaciones con conexión a db*/
        try {
            //String host = "jdbc:mysql://192.168.35.25:3306/mariodb?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC";
            System.out.println("Conectando MSSQL V12...");
            //String host = "jdbc:sqlserver://213.149.235.131:49412;databaseName=x3famesa;schema=FAMESAOF";
            String host = "jdbc:sqlserver://192.168.28.100:51439;databaseName=x3;schema=LIVE";
            String uName = "it";
            String uPass = "@Famesa123";
            con = DriverManager.getConnection(host, uName, uPass);

            //Statement stmt = con.createStatement();//esta conexión sólo permite navegar con los registros hacia adelante
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
           
        } catch (SQLException err) {
            System.out.println(err.getMessage());
        }
    }
    
    public void DoDesconnect(){
           //Desconcectar de la BBDD si existiese
            if (con != null) {

            try {
                System.out.println("desconectando db...");
                con.close();
                if (stmt != null) {
                
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(null, ex);

            }

        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jPanel6 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jText_referencia = new javax.swing.JTextField();
        jButton_consultar_registro = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jText_ref_desc_cte = new javax.swing.JTextField();
        jText_ref_ean = new javax.swing.JTextField();
        jText_ref_desc_art = new javax.swing.JTextField();
        jCombo_ref = new javax.swing.JComboBox();
        jText_ref_cliente = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jFormattedTextField_cantidad = new javax.swing.JFormattedTextField();
        jButton4 = new javax.swing.JButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jText_ref_capacidad = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jLabel_logo2 = new javax.swing.JLabel();
        buttonGroup_idioma = new javax.swing.ButtonGroup();
        jDialog2 = new javax.swing.JDialog();
        jPanel10 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jCombo_pedpro = new javax.swing.JComboBox();
        jText_pedproNumped = new javax.swing.JTextField();
        jText_pedproProv = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTb = new javax.swing.JTable();
        jText_empresa = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jLabel_logo3 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        buttonGroup_idioma_alb = new javax.swing.ButtonGroup();
        jDialog3 = new javax.swing.JDialog();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jTextField_AlbCte = new javax.swing.JTextField();
        jComboBox_AlbNormal = new javax.swing.JComboBox();
        jTextField_AlbPedidoCte = new javax.swing.JTextField();
        jTextField_AlbTrans = new javax.swing.JTextField();
        jTextField_AlbDirEnv = new javax.swing.JTextField();
        jTextField_AlbBultos = new javax.swing.JTextField();
        jTextField_AlbNumAlb = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jButton_ConsultaAlbNormal = new javax.swing.JButton();
        jTextField_AlbNomCte = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField_AlbDir2 = new javax.swing.JTextField();
        jTextField_AlbDirPostal = new javax.swing.JTextField();
        jButton_RefrescarAlb = new javax.swing.JButton();
        jTextField_AlbPrep = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jRadioButton_alb_fam = new javax.swing.JRadioButton();
        jRadioButton_alb_tri = new javax.swing.JRadioButton();
        jLayeredPane3 = new javax.swing.JLayeredPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        jLabel14 = new javax.swing.JLabel();
        jDialog4 = new javax.swing.JDialog();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        jFormattedTextField_cantidad1 = new javax.swing.JFormattedTextField();
        jLabel30 = new javax.swing.JLabel();
        jFormattedTextField_fontSize = new javax.swing.JFormattedTextField();
        jFormattedTextField_fontSize1 = new javax.swing.JFormattedTextField();
        jFormattedTextField_fontSize2 = new javax.swing.JFormattedTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jTonta_texto = new javax.swing.JTextField();
        jTonta_texto1 = new javax.swing.JTextField();
        jTonta_texto2 = new javax.swing.JTextField();
        jTonta_texto3 = new javax.swing.JTextField();
        jTonta_texto4 = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLayeredPane4 = new javax.swing.JLayeredPane();
        jLabel_logo4 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextArea5 = new javax.swing.JTextArea();
        jDialog5 = new javax.swing.JDialog();
        jPanel17 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jFormattedTextField_CantOF = new javax.swing.JFormattedTextField();
        jPanel28 = new javax.swing.JPanel();
        jFormattedTextField_CantPadreCaja = new javax.swing.JFormattedTextField();
        jFormattedTextField_CantPadrePallet = new javax.swing.JFormattedTextField();
        jLabel42 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jPanel29 = new javax.swing.JPanel();
        jFormattedTextField_CantPallet = new javax.swing.JFormattedTextField();
        jLabel43 = new javax.swing.JLabel();
        jFormattedTextField_CantCaja = new javax.swing.JFormattedTextField();
        jLabel38 = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        jTextField_PadreArti = new javax.swing.JTextField();
        jTextField_PadreDesc = new javax.swing.JTextField();
        jTextField_PadreEan = new javax.swing.JTextField();
        jComboBox_Padres = new javax.swing.JComboBox();
        jPanel21 = new javax.swing.JPanel();
        jComboBox_OF = new javax.swing.JComboBox();
        jLabel23 = new javax.swing.JLabel();
        jTextField_OF = new javax.swing.JTextField();
        jButton_ConsultaOF = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jTextField_OFarti = new javax.swing.JTextField();
        jTextField_OfEan = new javax.swing.JTextField();
        jTextField_OFdescrip = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jTextField_OfLote = new javax.swing.JTextField();
        jTextField_OfKit = new javax.swing.JTextField();
        jLayeredPane5 = new javax.swing.JLayeredPane();
        jLabel40 = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTextArea6 = new javax.swing.JTextArea();
        jPanel31 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTextArea_NetUse = new javax.swing.JTextArea();
        jPanel22 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        jRadioButton_Logo = new javax.swing.JRadioButton();
        jPanel25 = new javax.swing.JPanel();
        jRadioButton_Producto = new javax.swing.JRadioButton();
        jRadioButton_Caja = new javax.swing.JRadioButton();
        jRadioButton_Pallet = new javax.swing.JRadioButton();
        jRadioButton_Kety = new javax.swing.JRadioButton();
        jPanel26 = new javax.swing.JPanel();
        jRadioButton_OF = new javax.swing.JRadioButton();
        jRadioButton_Padre = new javax.swing.JRadioButton();
        jPanel27 = new javax.swing.JPanel();
        jRadioButton_Zebra1 = new javax.swing.JRadioButton();
        jRadioButton_Godex1 = new javax.swing.JRadioButton();
        jRadioButton_Godex2 = new javax.swing.JRadioButton();
        jRadioButton_Zebra2 = new javax.swing.JRadioButton();
        jPanel30 = new javax.swing.JPanel();
        jFormattedTextField_Pico = new javax.swing.JFormattedTextField();
        jFormattedTextField_CapPallet = new javax.swing.JFormattedTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jFormattedTextField_CapCaja = new javax.swing.JFormattedTextField();
        jLabel44 = new javax.swing.JLabel();
        jFormattedTextField_PicoCaja = new javax.swing.JFormattedTextField();
        jLabel45 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton_RefrescarAlb1 = new javax.swing.JButton();
        jDialog6 = new javax.swing.JDialog();
        jPanel32 = new javax.swing.JPanel();
        jPanel33 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        jPanel34 = new javax.swing.JPanel();
        jFormattedTextField_fontSize3 = new javax.swing.JFormattedTextField();
        jLabel47 = new javax.swing.JLabel();
        jPanel36 = new javax.swing.JPanel();
        jRadioButton_UbiFam = new javax.swing.JRadioButton();
        jRadioButton_UbiTri = new javax.swing.JRadioButton();
        jComboBox_Ubi = new javax.swing.JComboBox();
        jButton_UbiConsulta = new javax.swing.JButton();
        jPanel37 = new javax.swing.JPanel();
        jText_UbiHasta = new javax.swing.JTextField();
        jText_UbiDesde = new javax.swing.JTextField();
        jLabel51 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jFormattedTextField_cantidadUbi = new javax.swing.JFormattedTextField();
        jLabel46 = new javax.swing.JLabel();
        jPanel35 = new javax.swing.JPanel();
        jLayeredPane6 = new javax.swing.JLayeredPane();
        jLabel_logo5 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTextArea7 = new javax.swing.JTextArea();
        buttonGroup_DatosAImp = new javax.swing.ButtonGroup();
        buttonGroup_Impresoras = new javax.swing.ButtonGroup();
        buttonGroup_TipoEti = new javax.swing.ButtonGroup();
        jRadioButton_Pallet.setSelected(true);
        buttonGroup_Ubicaciones = new javax.swing.ButtonGroup();
        buttonGroup_ReferenciaSimple = new javax.swing.ButtonGroup();
        buttonGroup_stocks = new javax.swing.ButtonGroup();
        jMenuItem2 = new javax.swing.JMenuItem();
        jDialog7 = new javax.swing.JDialog();
        jPanel38 = new javax.swing.JPanel();
        jPanel39 = new javax.swing.JPanel();
        jPanel41 = new javax.swing.JPanel();
        jText_EtqSimple_Ean = new javax.swing.JTextField();
        jText_EtqSimple_descrip = new javax.swing.JTextField();
        jText_EtqSimple_ref = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jTextField_EtqSimple_lan = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        jPanel42 = new javax.swing.JPanel();
        jButton_EtqSimple_Imprimir = new javax.swing.JButton();
        jButton_EtqSimple_Generar = new javax.swing.JButton();
        jPanel43 = new javax.swing.JPanel();
        jCombo_EtqSimple_ref = new javax.swing.JComboBox();
        jLabel56 = new javax.swing.JLabel();
        jFormattedTextField_CantRef = new javax.swing.JFormattedTextField();
        jPanel40 = new javax.swing.JPanel();
        jLayeredPane7 = new javax.swing.JLayeredPane();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTextArea8 = new javax.swing.JTextArea();
        jLabel_logo6 = new javax.swing.JLabel();
        jDialog8 = new javax.swing.JDialog();
        jPanel44 = new javax.swing.JPanel();
        jPanel45 = new javax.swing.JPanel();
        jPanel46 = new javax.swing.JPanel();
        jText_ElCorte_RefCte = new javax.swing.JTextField();
        jText_ElCorte_Descrip = new javax.swing.JTextField();
        jLabel53 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jTextField_ElCorte_Color = new javax.swing.JTextField();
        jLabel58 = new javax.swing.JLabel();
        jTextField_ElCorte_UdCaja = new javax.swing.JTextField();
        jLabel62 = new javax.swing.JLabel();
        jTextField_ElCorte_Sucursal = new javax.swing.JTextField();
        jLabel63 = new javax.swing.JLabel();
        jTextField_ElCorte_Depart = new javax.swing.JTextField();
        jLabel64 = new javax.swing.JLabel();
        jTextField_ElCorte_Ped = new javax.swing.JTextField();
        jLabel65 = new javax.swing.JLabel();
        jTextField_ElCorte_Alb2 = new javax.swing.JTextField();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jPanel47 = new javax.swing.JPanel();
        jButton_ElCorte_Print = new javax.swing.JButton();
        jButton_ElCorte_Generar = new javax.swing.JButton();
        jLabel49 = new javax.swing.JLabel();
        jText_ElCorte_Alb = new javax.swing.JTextField();
        jTextField_ElCorte_Art = new javax.swing.JTextField();
        jLabel61 = new javax.swing.JLabel();
        jPanel48 = new javax.swing.JPanel();
        jCombo_ElCorte = new javax.swing.JComboBox();
        jLabel59 = new javax.swing.JLabel();
        jFormattedTextField_CantElCorte = new javax.swing.JFormattedTextField();
        jTextField_ElCorte_Cte = new javax.swing.JTextField();
        jLabel60 = new javax.swing.JLabel();
        jPanel49 = new javax.swing.JPanel();
        jLayeredPane8 = new javax.swing.JLayeredPane();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTextArea9 = new javax.swing.JTextArea();
        jLabel_logo7 = new javax.swing.JLabel();
        jDialog9 = new javax.swing.JDialog();
        jPanel50 = new javax.swing.JPanel();
        jPanel51 = new javax.swing.JPanel();
        jButton9 = new javax.swing.JButton();
        jPanel52 = new javax.swing.JPanel();
        jFormattedTextField_fontSize4 = new javax.swing.JFormattedTextField();
        jLabel68 = new javax.swing.JLabel();
        jPanel53 = new javax.swing.JPanel();
        jRadioButton_PlantaF = new javax.swing.JRadioButton();
        jRadioButton_PlantaT = new javax.swing.JRadioButton();
        jComboBox_Stock = new javax.swing.JComboBox();
        jButton_ItmLot = new javax.swing.JButton();
        jTextStock_arti = new javax.swing.JTextField();
        jTextStock_lote = new javax.swing.JTextField();
        jPanel54 = new javax.swing.JPanel();
        jText_StockHasta = new javax.swing.JTextField();
        jText_StockDesde = new javax.swing.JTextField();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jFormattedTextField_cantidadUbi1 = new javax.swing.JFormattedTextField();
        jLabel71 = new javax.swing.JLabel();
        jPanel55 = new javax.swing.JPanel();
        jLayeredPane9 = new javax.swing.JLayeredPane();
        jLabel_logo8 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTextArea10 = new javax.swing.JTextArea();
        buttonGroup_alb = new javax.swing.ButtonGroup();
        jDialog10 = new javax.swing.JDialog();
        jPanel56 = new javax.swing.JPanel();
        jPanel57 = new javax.swing.JPanel();
        jText_alb_dascher = new javax.swing.JTextField();
        jLabel72 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTb_dascher = new javax.swing.JTable();
        jText_empresa1 = new javax.swing.JTextField();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jRadioButton_DASCHER = new javax.swing.JRadioButton();
        jRadioButton_XPO = new javax.swing.JRadioButton();
        jRadioButton_MRW = new javax.swing.JRadioButton();
        jLabel85 = new javax.swing.JLabel();
        jLayeredPane10 = new javax.swing.JLayeredPane();
        jScrollPane15 = new javax.swing.JScrollPane();
        jTextArea11 = new javax.swing.JTextArea();
        jLabel_logo9 = new javax.swing.JLabel();
        jPanel58 = new javax.swing.JPanel();
        jScrollPane16 = new javax.swing.JScrollPane();
        jTextArea_xpo = new javax.swing.JTextArea();
        buttonGroup_tipo_transportista = new javax.swing.ButtonGroup();
        jDialog11 = new javax.swing.JDialog();
        jPanel59 = new javax.swing.JPanel();
        jPanel60 = new javax.swing.JPanel();
        jTextField_AlbCte1 = new javax.swing.JTextField();
        jComboBox_AlbSSCC = new javax.swing.JComboBox();
        jTextField_AlbPedidoCte1 = new javax.swing.JTextField();
        jTextField_AlbTrans1 = new javax.swing.JTextField();
        jTextField_AlbDirEnv1 = new javax.swing.JTextField();
        jTextField_AlbBultos1 = new javax.swing.JTextField();
        jTextField_AlbNumAlb1 = new javax.swing.JTextField();
        jLabel73 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jButton_ConsultaAlbNormal1 = new javax.swing.JButton();
        jTextField_AlbNomCte1 = new javax.swing.JTextField();
        jLabel81 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jTextField_AlbDir3 = new javax.swing.JTextField();
        jTextField_AlbDirPostal1 = new javax.swing.JTextField();
        jButton_RefrescarAlb2 = new javax.swing.JButton();
        jTextField_AlbPrep1 = new javax.swing.JTextField();
        jLabel82 = new javax.swing.JLabel();
        jRadioButton_alb_fam1 = new javax.swing.JRadioButton();
        jRadioButton_alb_tri1 = new javax.swing.JRadioButton();
        jTextField_SSCC_pallets = new javax.swing.JTextField();
        jLabel84 = new javax.swing.JLabel();
        jLayeredPane11 = new javax.swing.JLayeredPane();
        jScrollPane17 = new javax.swing.JScrollPane();
        jTextArea12 = new javax.swing.JTextArea();
        jLabel83 = new javax.swing.JLabel();
        buttonGroup_SSCC = new javax.swing.ButtonGroup();
        jPanel7 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jText_albaran = new javax.swing.JTextField();
        jLabel_albaran = new javax.swing.JLabel();
        jButton_consultar = new javax.swing.JButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea_log = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu_ir = new javax.swing.JMenu();
        jMenuItem_albaranes = new javax.swing.JMenuItem();
        jMenuItem_albaranNormal = new javax.swing.JMenuItem();
        jMenuItem_referencias = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_tonta = new javax.swing.JMenuItem();
        jMenuItem_OF = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_alb_dascher = new javax.swing.JMenuItem();
        jMenuItem_SSCC = new javax.swing.JMenuItem();
        jMenuItem_salir = new javax.swing.JMenuItem();

        jDialog1.setTitle("Referencias");
        jDialog1.setBackground(new java.awt.Color(240, 181, 81));
        jDialog1.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog1.setForeground(java.awt.Color.gray);
        jDialog1.setLocation(new java.awt.Point(0, 0));
        jDialog1.setResizable(false);
        jDialog1.setSize(new java.awt.Dimension(790, 620));
        jDialog1.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog1WindowClosed(evt);
            }
        });

        jPanel6.setBackground(new java.awt.Color(240, 232, 150));

        jPanel4.setBackground(new java.awt.Color(141, 191, 72));

        jText_referencia.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jText_referencia.setToolTipText("Introduce referencia para hacer consulta");

        jButton_consultar_registro.setText("Generar");
        jButton_consultar_registro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_consultar_registroActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Referencia");

        jText_ref_desc_cte.setEnabled(false);

        jText_ref_ean.setEnabled(false);

        jText_ref_desc_art.setEnabled(false);

        jCombo_ref.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "REFERENCIA CLIENTE" }));
        jCombo_ref.setToolTipText("Despliega para ver referencia + cliente");
        jCombo_ref.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCombo_refItemStateChanged(evt);
            }
        });
        jCombo_ref.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCombo_refMouseClicked(evt);
            }
        });
        jCombo_ref.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCombo_refActionPerformed(evt);
            }
        });

        jText_ref_cliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jText_ref_cliente.setToolTipText("Introduce cód. cliente para hacer consulta");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Cód. Cliente");

        jButton3.setText("Imprimir");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Descripción Artículo");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Nombre Cliente");
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("EAN");
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Selección rápida");

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel8.setText("Núm. etiquetas");

        jFormattedTextField_cantidad.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_cantidad.setText("1");
        jFormattedTextField_cantidad.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_cantidad.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_cantidadFocusLost(evt);
            }
        });
        jFormattedTextField_cantidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_cantidadActionPerformed(evt);
            }
        });

        jButton4.setText("Limpiar");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jRadioButton1.setBackground(jDialog1.getBackground());
        buttonGroup_idioma.add(jRadioButton1);
        jRadioButton1.setText("SPA");
        jRadioButton1.setToolTipText("Texto en español");
        jRadioButton1.setName("SPA"); // NOI18N

        jRadioButton2.setBackground(jDialog1.getBackground());
        buttonGroup_idioma.add(jRadioButton2);
        jRadioButton2.setText("ENG");
        jRadioButton2.setToolTipText("Texto en inglés");
        jRadioButton2.setName("ENG"); // NOI18N

        jRadioButton3.setBackground(jDialog1.getBackground());
        buttonGroup_idioma.add(jRadioButton3);
        jRadioButton3.setText("FRA");
        jRadioButton3.setToolTipText("Texto en francés");
        jRadioButton3.setName("FRA"); // NOI18N

        jText_ref_capacidad.setEnabled(false);

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Capacidad");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Idioma");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jText_referencia)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                                    .addComponent(jText_ref_cliente))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jText_ref_desc_cte, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jText_ref_desc_art, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                            .addComponent(jText_ref_ean, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jText_ref_capacidad)
                                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton_consultar_registro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jFormattedTextField_cantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jRadioButton1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jRadioButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(5, 5, 5)
                                        .addComponent(jRadioButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(49, 49, 49)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jCombo_ref, 0, 1, Short.MAX_VALUE))))
                        .addGap(72, 72, 72))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel10))
                .addGap(1, 1, 1)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_cantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jCombo_ref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_referencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jText_ref_cliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jText_ref_desc_cte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addGap(1, 1, 1)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jText_ref_desc_art, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9))
                        .addGap(1, 1, 1)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jText_ref_ean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jText_ref_capacidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton_consultar_registro)
                            .addComponent(jButton4))
                        .addGap(14, 14, 14)
                        .addComponent(jButton3)))
                .addGap(31, 31, 31))
        );

        jPanel5.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLayeredPane1.setBackground(new java.awt.Color(141, 191, 72));

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jTextArea2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jScrollPane2.setViewportView(jTextArea2);
        jTextArea2.getAccessibleContext().setAccessibleName("");

        jLabel_logo2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_logo2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel_logo2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jLayeredPane1.setLayer(jScrollPane2, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel_logo2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLayeredPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jDialog2.setTitle("Pedidos a proveedor");
        jDialog2.setBackground(new java.awt.Color(204, 255, 51));
        jDialog2.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog2.setLocation(new java.awt.Point(0, 0));
        jDialog2.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog2WindowClosed(evt);
            }
        });

        jPanel10.setBackground(new java.awt.Color(240, 232, 150));

        jPanel8.setBackground(new java.awt.Color(141, 191, 72));

        jCombo_pedpro.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pedido Proveedor" }));
        jCombo_pedpro.setToolTipText("Despliega para ver pedido + proveedor");
        jCombo_pedpro.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCombo_pedproItemStateChanged(evt);
            }
        });
        jCombo_pedpro.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCombo_pedproMouseClicked(evt);
            }
        });
        jCombo_pedpro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCombo_pedproActionPerformed(evt);
            }
        });

        jText_pedproNumped.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jText_pedproNumped.setToolTipText("Introduce el pedido y pulsa \"ENTER\"");
        jText_pedproNumped.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jText_pedproNumpedActionPerformed(evt);
            }
        });
        jText_pedproNumped.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jText_pedproNumpedKeyTyped(evt);
            }
        });

        jText_pedproProv.setEnabled(false);

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("Nº de pedido");

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Proveedor");

        jButton5.setText("Imprimir");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jTb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Artículo", "Unidades", "Dun14", "Capacidad", "Cod EAN", "Info Lote", "Desc. Articulo", "Seleccionado"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTb);

        jText_empresa.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jText_empresa.setText("FABRICANTES DE MENAJE, S.A");
        jText_empresa.setToolTipText("Nombre de empresa a imprimir");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setText("Empresa");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jScrollPane4)
                        .addContainerGap())
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jText_pedproProv))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jText_pedproNumped, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jCombo_pedpro, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jText_empresa, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCombo_pedpro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jText_pedproNumped, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jText_pedproProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jText_empresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addContainerGap())
        );

        jLayeredPane2.setBackground(new java.awt.Color(141, 191, 72));

        jTextArea3.setEditable(false);
        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jTextArea3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jScrollPane3.setViewportView(jTextArea3);

        jLabel_logo3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        jPanel9.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jLayeredPane2Layout = new javax.swing.GroupLayout(jLayeredPane2);
        jLayeredPane2.setLayout(jLayeredPane2Layout);
        jLayeredPane2Layout.setHorizontalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane2Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_logo3, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jLayeredPane2Layout.setVerticalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_logo3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(148, 148, 148))
        );
        jLayeredPane2.setLayer(jScrollPane3, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jLabel_logo3, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jPanel9, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLayeredPane2))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jDialog2Layout = new javax.swing.GroupLayout(jDialog2.getContentPane());
        jDialog2.getContentPane().setLayout(jDialog2Layout);
        jDialog2Layout.setHorizontalGroup(
            jDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog2Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialog2Layout.setVerticalGroup(
            jDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog2Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 564, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        jDialog3.setTitle("Impresión de etiquetas de pedidos");
        jDialog3.setBackground(new java.awt.Color(255, 153, 51));
        jDialog3.setBounds(new java.awt.Rectangle(550, 600, 815, 600));
        jDialog3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog3.setLocation(new java.awt.Point(0, 0));
        jDialog3.setPreferredSize(new java.awt.Dimension(761, 575));

        jPanel11.setBackground(new java.awt.Color(240, 232, 150));
        jPanel11.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel11.setEnabled(false);

        jPanel12.setBackground(new java.awt.Color(141, 191, 72));

        jTextField_AlbCte.setEditable(false);
        jTextField_AlbCte.setEnabled(false);

        jComboBox_AlbNormal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ALB DENOX" }));
        jComboBox_AlbNormal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_AlbNormalActionPerformed(evt);
            }
        });

        jTextField_AlbPedidoCte.setEditable(false);
        jTextField_AlbPedidoCte.setEnabled(false);

        jTextField_AlbTrans.setEditable(false);
        jTextField_AlbTrans.setEnabled(false);

        jTextField_AlbDirEnv.setEditable(false);
        jTextField_AlbDirEnv.setEnabled(false);

        jTextField_AlbBultos.setEditable(false);
        jTextField_AlbBultos.setEnabled(false);
        jTextField_AlbBultos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_AlbBultosActionPerformed(evt);
            }
        });

        jLabel15.setText("Núm. Albarán:");

        jLabel16.setText("Cód. Cliente:");

        jLabel17.setText("Pedido Cliente:");

        jLabel18.setText("Transportista:");

        jLabel19.setText("Dir. Envío:");

        jLabel20.setText("Núm. Bultos:");

        jButton_ConsultaAlbNormal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_ConsultaAlbNormal.setText("Consulta");
        jButton_ConsultaAlbNormal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ConsultaAlbNormalActionPerformed(evt);
            }
        });

        jTextField_AlbNomCte.setEditable(false);
        jTextField_AlbNomCte.setEnabled(false);

        jLabel21.setText("Nombre Cliente:");

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton1.setText("IMPRIMIR");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField_AlbDir2.setEnabled(false);

        jTextField_AlbDirPostal.setEnabled(false);

        jButton_RefrescarAlb.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_RefrescarAlb.setText("REFRESCAR");
        jButton_RefrescarAlb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RefrescarAlbActionPerformed(evt);
            }
        });

        jTextField_AlbPrep.setEnabled(false);
        jTextField_AlbPrep.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel22.setText("Preparador:");

        buttonGroup_alb.add(jRadioButton_alb_fam);
        jRadioButton_alb_fam.setSelected(true);
        jRadioButton_alb_fam.setText("FAMESA");
        jRadioButton_alb_fam.setActionCommand("FAM");
        jRadioButton_alb_fam.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_alb_famItemStateChanged(evt);
            }
        });

        buttonGroup_alb.add(jRadioButton_alb_tri);
        jRadioButton_alb_tri.setText("TRILLA");
        jRadioButton_alb_tri.setActionCommand("TRI");
        jRadioButton_alb_tri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_alb_triActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(7, 7, 7))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
                                .addGap(18, 18, Short.MAX_VALUE)))
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_AlbBultos, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_AlbPedidoCte, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_AlbCte, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jTextField_AlbNumAlb, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jButton_ConsultaAlbNormal))
                            .addComponent(jTextField_AlbTrans, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton_RefrescarAlb, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(150, 150, 150))
                            .addComponent(jTextField_AlbDir2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_AlbDirEnv, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField_AlbPrep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField_AlbDirPostal, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel12Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jRadioButton_alb_fam, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButton_alb_tri, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jComboBox_AlbNormal, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_AlbNomCte, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton_alb_fam)
                    .addComponent(jRadioButton_alb_tri))
                .addGap(11, 11, 11)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jComboBox_AlbNormal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbNumAlb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(jButton_ConsultaAlbNormal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbCte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbPedidoCte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbTrans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18)))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbNomCte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbDirEnv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_AlbDir2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_AlbDirPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbPrep, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel22))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jTextField_AlbBultos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(jButton_RefrescarAlb))
                .addGap(0, 9, Short.MAX_VALUE))
        );

        jTextArea4.setColumns(20);
        jTextArea4.setRows(5);
        jTextArea4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jTextArea4.setEnabled(false);
        jTextArea4.setName(""); // NOI18N
        jScrollPane6.setViewportView(jTextArea4);

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        javax.swing.GroupLayout jLayeredPane3Layout = new javax.swing.GroupLayout(jLayeredPane3);
        jLayeredPane3.setLayout(jLayeredPane3Layout);
        jLayeredPane3Layout.setHorizontalGroup(
            jLayeredPane3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jLayeredPane3Layout.setVerticalGroup(
            jLayeredPane3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane3Layout.createSequentialGroup()
                .addGroup(jLayeredPane3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane3Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jLayeredPane3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        jLayeredPane3.setLayer(jScrollPane6, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane3.setLayer(jLabel14, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLayeredPane3))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout jDialog3Layout = new javax.swing.GroupLayout(jDialog3.getContentPane());
        jDialog3.getContentPane().setLayout(jDialog3Layout);
        jDialog3Layout.setHorizontalGroup(
            jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jDialog3Layout.setVerticalGroup(
            jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel11.getAccessibleContext().setAccessibleName("ZPL");

        jDialog3.getAccessibleContext().setAccessibleName("Impresión de etiquetas de albarán");

        jDialog4.setTitle("Referencias");
        jDialog4.setBackground(new java.awt.Color(240, 181, 81));
        jDialog4.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog4.setForeground(java.awt.Color.gray);
        jDialog4.setLocation(new java.awt.Point(0, 0));
        jDialog4.setSize(new java.awt.Dimension(790, 620));
        jDialog4.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog4WindowClosed(evt);
            }
        });

        jPanel13.setBackground(new java.awt.Color(240, 232, 150));

        jPanel14.setBackground(new java.awt.Color(141, 191, 72));

        jButton6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton6.setText("Imprimir");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel29.setText("Núm. etiquetas");

        jFormattedTextField_cantidad1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_cantidad1.setText("1");
        jFormattedTextField_cantidad1.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_cantidad1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_cantidad1FocusLost(evt);
            }
        });
        jFormattedTextField_cantidad1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_cantidad1ActionPerformed(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel30.setText("Tamaño fuente");

        jFormattedTextField_fontSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_fontSize.setText("164");
        jFormattedTextField_fontSize.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_fontSize.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_fontSizeFocusLost(evt);
            }
        });
        jFormattedTextField_fontSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_fontSizeActionPerformed(evt);
            }
        });
        jFormattedTextField_fontSize.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jFormattedTextField_fontSizeKeyTyped(evt);
            }
        });

        jFormattedTextField_fontSize1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_fontSize1.setText("200");
        jFormattedTextField_fontSize1.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_fontSize1.setEnabled(false);
        jFormattedTextField_fontSize1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_fontSize1FocusLost(evt);
            }
        });
        jFormattedTextField_fontSize1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_fontSize1ActionPerformed(evt);
            }
        });
        jFormattedTextField_fontSize1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jFormattedTextField_fontSize1KeyTyped(evt);
            }
        });

        jFormattedTextField_fontSize2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_fontSize2.setText("400");
        jFormattedTextField_fontSize2.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_fontSize2.setEnabled(false);
        jFormattedTextField_fontSize2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_fontSize2FocusLost(evt);
            }
        });
        jFormattedTextField_fontSize2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_fontSize2ActionPerformed(evt);
            }
        });
        jFormattedTextField_fontSize2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jFormattedTextField_fontSize2KeyTyped(evt);
            }
        });

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel31.setText("Eje X");

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel32.setText("Eje Y");

        jPanel16.setBackground(new java.awt.Color(141, 191, 72));

        jTonta_texto4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTonta_texto4ActionPerformed(evt);
            }
        });

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel33.setText("Texto lin. 1");

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel34.setText("Texto lin. 2");

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel35.setText("Texto lin. 3");

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel36.setText("Texto lin. 4");

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel37.setText("Texto lin. 5");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                        .addComponent(jLabel35, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel34, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel37, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTonta_texto4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addComponent(jTonta_texto3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTonta_texto2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTonta_texto1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTonta_texto, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTonta_texto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel33))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTonta_texto1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTonta_texto2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTonta_texto3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTonta_texto4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFormattedTextField_cantidad1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField_fontSize, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFormattedTextField_fontSize2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField_fontSize1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6)
                .addGap(45, 45, 45))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jFormattedTextField_fontSize1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jFormattedTextField_fontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel30))
                                .addGroup(jPanel14Layout.createSequentialGroup()
                                    .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel31)
                                        .addComponent(jFormattedTextField_cantidad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel29))
                                    .addGap(18, 18, 18)
                                    .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel32)
                                        .addComponent(jFormattedTextField_fontSize2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(1, 1, 1)))))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton6)
                .addGap(22, 22, 22))
        );

        jPanel15.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLayeredPane4.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jLayeredPane4Layout = new javax.swing.GroupLayout(jLayeredPane4);
        jLayeredPane4.setLayout(jLayeredPane4Layout);
        jLayeredPane4Layout.setHorizontalGroup(
            jLayeredPane4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 148, Short.MAX_VALUE)
        );
        jLayeredPane4Layout.setVerticalGroup(
            jLayeredPane4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );

        jLabel_logo4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        jTextArea5.setColumns(20);
        jTextArea5.setRows(5);
        jTextArea5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jTextArea5.setEnabled(false);
        jScrollPane7.setViewportView(jTextArea5);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel_logo4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLayeredPane4))
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLayeredPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_logo4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane7))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jDialog4Layout = new javax.swing.GroupLayout(jDialog4.getContentPane());
        jDialog4.getContentPane().setLayout(jDialog4Layout);
        jDialog4Layout.setHorizontalGroup(
            jDialog4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialog4Layout.setVerticalGroup(
            jDialog4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jDialog5.setTitle("Impresión de etiquetas de producto");
        jDialog5.setBackground(new java.awt.Color(255, 153, 51));
        jDialog5.setBounds(new java.awt.Rectangle(750, 750, 800, 850));
        jDialog5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog5.setLocation(new java.awt.Point(0, 0));
        jDialog5.setResizable(false);
        jDialog5.setSize(new java.awt.Dimension(1115, 655));

        jPanel17.setBackground(new java.awt.Color(240, 232, 150));
        jPanel17.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel17.setEnabled(false);
        jPanel17.setPreferredSize(new java.awt.Dimension(746, 750));

        jPanel18.setBackground(new java.awt.Color(141, 191, 72));

        jPanel19.setBackground(new java.awt.Color(204, 255, 204));
        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cantidad a imprimir", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jPanel19.setName("UNIDADES ARTÍCULO OF"); // NOI18N

        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("Producto");

        jFormattedTextField_CantOF.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_CantOF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_CantOF.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jFormattedTextField_CantOF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CantOFActionPerformed(evt);
            }
        });
        jFormattedTextField_CantOF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_CantOFKeyPressed(evt);
            }
        });

        jPanel28.setBackground(new java.awt.Color(204, 255, 204));
        jPanel28.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Padre", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jPanel28.setPreferredSize(new java.awt.Dimension(188, 64));
        jPanel28.setRequestFocusEnabled(false);

        jFormattedTextField_CantPadreCaja.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_CantPadreCaja.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_CantPadreCaja.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jFormattedTextField_CantPadreCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CantPadreCajaActionPerformed(evt);
            }
        });
        jFormattedTextField_CantPadreCaja.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_CantPadreCajaKeyPressed(evt);
            }
        });

        jFormattedTextField_CantPadrePallet.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_CantPadrePallet.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_CantPadrePallet.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jFormattedTextField_CantPadrePallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CantPadrePalletActionPerformed(evt);
            }
        });
        jFormattedTextField_CantPadrePallet.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_CantPadrePalletKeyPressed(evt);
            }
        });

        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel42.setText("Pallets");

        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setText("Cajas");

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFormattedTextField_CantPadreCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField_CantPadrePallet, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(jLabel42))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_CantPadreCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField_CantPadrePallet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel29.setBackground(new java.awt.Color(204, 255, 204));
        jPanel29.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "OF", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        jFormattedTextField_CantPallet.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_CantPallet.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_CantPallet.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jFormattedTextField_CantPallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CantPalletActionPerformed(evt);
            }
        });
        jFormattedTextField_CantPallet.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_CantPalletKeyPressed(evt);
            }
        });

        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setText("Pallets");

        jFormattedTextField_CantCaja.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_CantCaja.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_CantCaja.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jFormattedTextField_CantCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CantCajaActionPerformed(evt);
            }
        });
        jFormattedTextField_CantCaja.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_CantCajaKeyPressed(evt);
            }
        });

        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setText("Cajas");

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel29Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFormattedTextField_CantCaja, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel43, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jFormattedTextField_CantPallet, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel29Layout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextField_CantPallet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel29Layout.createSequentialGroup()
                        .addComponent(jLabel38)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextField_CantCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(jPanel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(100, 100, 100)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jFormattedTextField_CantOF, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(77, 77, 77))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFormattedTextField_CantOF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel29, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .addComponent(jPanel28, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel20.setBackground(new java.awt.Color(255, 204, 204));
        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos Padre", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jPanel20.setName("PADRE"); // NOI18N
        jPanel20.setPreferredSize(new java.awt.Dimension(398, 117));
        jPanel20.setRequestFocusEnabled(false);

        jTextField_PadreArti.setEditable(false);
        jTextField_PadreArti.setEnabled(false);

        jTextField_PadreDesc.setEditable(false);
        jTextField_PadreDesc.setEnabled(false);

        jTextField_PadreEan.setEditable(false);
        jTextField_PadreEan.setEnabled(false);

        jComboBox_Padres.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Padres" }));
        jComboBox_Padres.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_PadresActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBox_Padres, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField_PadreDesc, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel20Layout.createSequentialGroup()
                        .addComponent(jTextField_PadreArti, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_PadreEan, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(116, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox_Padres, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_PadreArti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_PadreEan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField_PadreDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel21.setBackground(new java.awt.Color(141, 191, 72));
        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos OF", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        jComboBox_OF.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NumOF" }));
        jComboBox_OF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_OFActionPerformed(evt);
            }
        });

        jLabel23.setText("Nº OF:");

        jTextField_OF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_OFActionPerformed(evt);
            }
        });

        jButton_ConsultaOF.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_ConsultaOF.setText("Consultar");
        jButton_ConsultaOF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ConsultaOFActionPerformed(evt);
            }
        });

        jLabel24.setText("Artículo:");

        jTextField_OFarti.setEditable(false);
        jTextField_OFarti.setEnabled(false);

        jTextField_OfEan.setEditable(false);
        jTextField_OfEan.setEnabled(false);

        jTextField_OFdescrip.setEditable(false);
        jTextField_OFdescrip.setEnabled(false);

        jLabel26.setText("Descripción:");

        jLabel25.setText("Lote:");

        jTextField_OfLote.setEnabled(false);

        jTextField_OfKit.setEnabled(false);

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox_OF, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addComponent(jTextField_OF, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_ConsultaOF))
                            .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel21Layout.createSequentialGroup()
                                    .addComponent(jTextField_OfLote, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jTextField_OfKit))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel21Layout.createSequentialGroup()
                                    .addComponent(jTextField_OFarti, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jTextField_OfEan))
                                .addComponent(jTextField_OFdescrip, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(26, 26, 26)))
                .addContainerGap())
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jComboBox_OF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_OF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(jButton_ConsultaOF))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_OFarti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(jTextField_OfEan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_OFdescrip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_OfLote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(jTextField_OfKit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel40.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        jPanel23.setBackground(new java.awt.Color(240, 232, 150));

        jTextArea6.setColumns(20);
        jTextArea6.setRows(5);
        jTextArea6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jTextArea6.setEnabled(false);
        jTextArea6.setName(""); // NOI18N
        jScrollPane8.setViewportView(jTextArea6);

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 41, Short.MAX_VALUE))
        );

        jTextArea_NetUse.setEditable(false);
        jTextArea_NetUse.setBackground(new java.awt.Color(0, 0, 0));
        jTextArea_NetUse.setColumns(20);
        jTextArea_NetUse.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea_NetUse.setRows(5);
        jTextArea_NetUse.setText("consola ms-dos");
        jScrollPane9.setViewportView(jTextArea_NetUse);

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane9)
                .addContainerGap())
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jLayeredPane5Layout = new javax.swing.GroupLayout(jLayeredPane5);
        jLayeredPane5.setLayout(jLayeredPane5Layout);
        jLayeredPane5Layout.setHorizontalGroup(
            jLayeredPane5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jLayeredPane5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane5Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))
                    .addGroup(jLayeredPane5Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jLayeredPane5Layout.setVerticalGroup(
            jLayeredPane5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane5Layout.createSequentialGroup()
                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 25, Short.MAX_VALUE))
            .addGroup(jLayeredPane5Layout.createSequentialGroup()
                .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(59, Short.MAX_VALUE))
        );
        jLayeredPane5.setLayer(jLabel40, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane5.setLayer(jPanel23, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane5.setLayer(jPanel31, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLayeredPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLayeredPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(112, 112, 112))
        );

        jPanel22.setBackground(new java.awt.Color(204, 204, 255));

        jPanel24.setBackground(new java.awt.Color(102, 204, 255));
        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos Generales", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        jRadioButton_Logo.setText("LOGO");

        jPanel25.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tipo", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        buttonGroup_TipoEti.add(jRadioButton_Producto);
        jRadioButton_Producto.setText("Producto");
        jRadioButton_Producto.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_ProductoItemStateChanged(evt);
            }
        });
        jRadioButton_Producto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton_ProductoMouseClicked(evt);
            }
        });
        jRadioButton_Producto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_ProductoActionPerformed(evt);
            }
        });

        buttonGroup_TipoEti.add(jRadioButton_Caja);
        jRadioButton_Caja.setText("Caja");
        jRadioButton_Caja.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_CajaItemStateChanged(evt);
            }
        });
        jRadioButton_Caja.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton_CajaMouseClicked(evt);
            }
        });

        buttonGroup_TipoEti.add(jRadioButton_Pallet);
        jRadioButton_Pallet.setText("Pallet");
        jRadioButton_Pallet.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_PalletItemStateChanged(evt);
            }
        });
        jRadioButton_Pallet.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton_PalletMouseClicked(evt);
            }
        });

        buttonGroup_TipoEti.add(jRadioButton_Kety);
        jRadioButton_Kety.setText("Kety");
        jRadioButton_Kety.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_KetyItemStateChanged(evt);
            }
        });
        jRadioButton_Kety.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton_KetyMouseClicked(evt);
            }
        });
        jRadioButton_Kety.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_KetyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jRadioButton_Pallet, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton_Producto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton_Caja, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(18, 18, 18)
                .addComponent(jRadioButton_Kety, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton_Producto)
                    .addComponent(jRadioButton_Kety))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton_Caja)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton_Pallet))
        );

        jPanel26.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Clase", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        buttonGroup_DatosAImp.add(jRadioButton_OF);
        jRadioButton_OF.setSelected(true);
        jRadioButton_OF.setText("OF");
        jRadioButton_OF.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_OFItemStateChanged(evt);
            }
        });
        jRadioButton_OF.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton_OFMouseClicked(evt);
            }
        });

        buttonGroup_DatosAImp.add(jRadioButton_Padre);
        jRadioButton_Padre.setText("Padre");
        jRadioButton_Padre.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_PadreItemStateChanged(evt);
            }
        });
        jRadioButton_Padre.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton_PadreMouseClicked(evt);
            }
        });
        jRadioButton_Padre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_PadreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jRadioButton_Padre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton_OF, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addComponent(jRadioButton_OF)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton_Padre))
        );

        jPanel27.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Impresora", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        buttonGroup_Impresoras.add(jRadioButton_Zebra1);
        jRadioButton_Zebra1.setText("Zebra 1 (LPT1)");
        jRadioButton_Zebra1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_Zebra1ActionPerformed(evt);
            }
        });

        buttonGroup_Impresoras.add(jRadioButton_Godex1);
        jRadioButton_Godex1.setText("Godex 1 (LPT3)");
        jRadioButton_Godex1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_Godex1ActionPerformed(evt);
            }
        });

        buttonGroup_Impresoras.add(jRadioButton_Godex2);
        jRadioButton_Godex2.setText("Godex 2 (LPT4)");

        buttonGroup_Impresoras.add(jRadioButton_Zebra2);
        jRadioButton_Zebra2.setSelected(true);
        jRadioButton_Zebra2.setText("Zebra 2 (LPT2)");
        jRadioButton_Zebra2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_Zebra2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jRadioButton_Zebra1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton_Zebra2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton_Godex1, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(jRadioButton_Godex2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addComponent(jRadioButton_Zebra1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton_Zebra2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton_Godex1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton_Godex2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel30.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Uds. de Envasado", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jFormattedTextField_Pico.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_Pico.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_Pico.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jFormattedTextField_Pico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_PicoActionPerformed(evt);
            }
        });
        jFormattedTextField_Pico.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_PicoKeyPressed(evt);
            }
        });

        jFormattedTextField_CapPallet.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_CapPallet.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_CapPallet.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jFormattedTextField_CapPallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CapPalletActionPerformed(evt);
            }
        });
        jFormattedTextField_CapPallet.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_CapPalletKeyPressed(evt);
            }
        });

        jLabel27.setText("Uds. x Pallet");

        jLabel39.setText("Picos pallet");

        jFormattedTextField_CapCaja.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_CapCaja.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_CapCaja.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jFormattedTextField_CapCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CapCajaActionPerformed(evt);
            }
        });
        jFormattedTextField_CapCaja.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_CapCajaKeyPressed(evt);
            }
        });

        jLabel44.setText("Uds. x Caja");

        jFormattedTextField_PicoCaja.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        jFormattedTextField_PicoCaja.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextField_PicoCaja.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jFormattedTextField_PicoCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_PicoCajaActionPerformed(evt);
            }
        });
        jFormattedTextField_PicoCaja.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_PicoCajaKeyPressed(evt);
            }
        });

        jLabel45.setText("Picos caja");

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel30Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel39)
                    .addComponent(jLabel44)
                    .addComponent(jLabel45))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jFormattedTextField_PicoCaja, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                    .addComponent(jFormattedTextField_CapCaja)
                    .addComponent(jFormattedTextField_CapPallet)
                    .addComponent(jFormattedTextField_Pico))
                .addContainerGap())
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel30Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_CapPallet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_Pico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_CapCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel44))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_PicoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel45))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jButton7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton7.setText("IMPRIMIR");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton_RefrescarAlb1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_RefrescarAlb1.setText("REFRESCAR");
        jButton_RefrescarAlb1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RefrescarAlb1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel27, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(jRadioButton_Logo)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                        .addComponent(jButton_RefrescarAlb1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton7)))
                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButton_Logo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton_RefrescarAlb1)))
        );

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jDialog5Layout = new javax.swing.GroupLayout(jDialog5.getContentPane());
        jDialog5.getContentPane().setLayout(jDialog5Layout);
        jDialog5Layout.setHorizontalGroup(
            jDialog5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, 858, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jDialog5Layout.setVerticalGroup(
            jDialog5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialog5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jDialog6.setTitle("Ubicaciones");
        jDialog6.setBackground(new java.awt.Color(240, 181, 81));
        jDialog6.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog6.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog6.setForeground(java.awt.Color.gray);
        jDialog6.setLocation(new java.awt.Point(0, 0));
        jDialog6.setSize(new java.awt.Dimension(790, 620));
        jDialog6.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog6WindowClosed(evt);
            }
        });

        jPanel32.setBackground(new java.awt.Color(240, 232, 150));

        jPanel33.setBackground(new java.awt.Color(141, 191, 72));

        jButton8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton8.setText("Imprimir");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jPanel34.setBackground(new java.awt.Color(141, 191, 72));

        jFormattedTextField_fontSize3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_fontSize3.setText("128");
        jFormattedTextField_fontSize3.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_fontSize3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_fontSize3FocusLost(evt);
            }
        });
        jFormattedTextField_fontSize3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_fontSize3ActionPerformed(evt);
            }
        });
        jFormattedTextField_fontSize3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jFormattedTextField_fontSize3KeyTyped(evt);
            }
        });

        jLabel47.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel47.setText("Tamaño fuente");

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFormattedTextField_fontSize3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(178, Short.MAX_VALUE))
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_fontSize3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel47))
                .addGap(0, 79, Short.MAX_VALUE))
        );

        buttonGroup_Ubicaciones.add(jRadioButton_UbiFam);
        jRadioButton_UbiFam.setText("Famesa");
        jRadioButton_UbiFam.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_UbiFamItemStateChanged(evt);
            }
        });
        jRadioButton_UbiFam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_UbiFamActionPerformed(evt);
            }
        });

        buttonGroup_Ubicaciones.add(jRadioButton_UbiTri);
        jRadioButton_UbiTri.setText("Trilla");
        jRadioButton_UbiTri.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_UbiTriItemStateChanged(evt);
            }
        });

        jComboBox_Ubi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ubicaciones" }));
        jComboBox_Ubi.setToolTipText("");
        jComboBox_Ubi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jComboBox_UbiMouseClicked(evt);
            }
        });

        jButton_UbiConsulta.setText("Consulta");
        jButton_UbiConsulta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_UbiConsultaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel36Layout = new javax.swing.GroupLayout(jPanel36);
        jPanel36.setLayout(jPanel36Layout);
        jPanel36Layout.setHorizontalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel36Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton_UbiConsulta)
                    .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jRadioButton_UbiFam, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jRadioButton_UbiTri, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jComboBox_Ubi, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel36Layout.setVerticalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel36Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButton_UbiFam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton_UbiTri, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox_Ubi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton_UbiConsulta)
                .addContainerGap())
        );

        jText_UbiHasta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jText_UbiHastaActionPerformed(evt);
            }
        });

        jLabel51.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel51.setText("Ubicación Hasta:");

        jLabel50.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel50.setText("Ubicación desde:");

        jFormattedTextField_cantidadUbi.setEditable(false);
        jFormattedTextField_cantidadUbi.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_cantidadUbi.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_cantidadUbi.setText("1");
        jFormattedTextField_cantidadUbi.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_cantidadUbi.setEnabled(false);
        jFormattedTextField_cantidadUbi.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jFormattedTextField_cantidadUbi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_cantidadUbiFocusLost(evt);
            }
        });
        jFormattedTextField_cantidadUbi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_cantidadUbiActionPerformed(evt);
            }
        });

        jLabel46.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel46.setText("Núm. etiquetas");

        javax.swing.GroupLayout jPanel37Layout = new javax.swing.GroupLayout(jPanel37);
        jPanel37.setLayout(jPanel37Layout);
        jPanel37Layout.setHorizontalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel37Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel37Layout.createSequentialGroup()
                        .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel51, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jText_UbiHasta, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jText_UbiDesde, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel37Layout.createSequentialGroup()
                        .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jFormattedTextField_cantidadUbi, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel37Layout.setVerticalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel37Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_UbiDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel50))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_UbiHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel51))
                .addGap(18, 18, 18)
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_cantidadUbi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel46))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel33Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel33Layout.createSequentialGroup()
                        .addComponent(jPanel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(134, 134, 134))
        );
        jPanel33Layout.setVerticalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel33Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton8)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel33Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        jPanel35.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel35Layout.setVerticalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLayeredPane6.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jLayeredPane6Layout = new javax.swing.GroupLayout(jLayeredPane6);
        jLayeredPane6.setLayout(jLayeredPane6Layout);
        jLayeredPane6Layout.setHorizontalGroup(
            jLayeredPane6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jLayeredPane6Layout.setVerticalGroup(
            jLayeredPane6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );

        jLabel_logo5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        jTextArea7.setColumns(20);
        jTextArea7.setRows(5);
        jTextArea7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jTextArea7.setEnabled(false);
        jScrollPane10.setViewportView(jTextArea7);

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel32Layout.createSequentialGroup()
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel_logo5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLayeredPane6))
                    .addComponent(jPanel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel32Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLayeredPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel32Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_logo5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane10))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jDialog6Layout = new javax.swing.GroupLayout(jDialog6.getContentPane());
        jDialog6.getContentPane().setLayout(jDialog6Layout);
        jDialog6Layout.setHorizontalGroup(
            jDialog6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialog6Layout.setVerticalGroup(
            jDialog6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jMenuItem2.setText("jMenuItem2");

        jDialog7.setTitle("Referencias");
        jDialog7.setBackground(new java.awt.Color(240, 181, 81));
        jDialog7.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog7.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog7.setForeground(java.awt.Color.gray);
        jDialog7.setLocation(new java.awt.Point(0, 0));
        jDialog7.setResizable(false);
        jDialog7.setSize(new java.awt.Dimension(790, 620));
        jDialog7.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog7WindowClosed(evt);
            }
        });

        jPanel38.setBackground(new java.awt.Color(240, 232, 150));

        jPanel39.setBackground(new java.awt.Color(141, 191, 72));

        jPanel41.setBackground(new java.awt.Color(141, 191, 72));
        jPanel41.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jText_EtqSimple_Ean.setEditable(false);
        jText_EtqSimple_Ean.setEnabled(false);

        jText_EtqSimple_descrip.setEditable(false);
        jText_EtqSimple_descrip.setEnabled(false);

        jText_EtqSimple_ref.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jText_EtqSimple_ref.setToolTipText("Introduce referencia para hacer consulta");

        jLabel48.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel48.setText("Referencia");
        jLabel48.setMaximumSize(new java.awt.Dimension(21, 13));
        jLabel48.setMinimumSize(new java.awt.Dimension(21, 13));
        jLabel48.setPreferredSize(new java.awt.Dimension(21, 13));

        jLabel52.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel52.setText("Descripción");
        jLabel52.setMaximumSize(new java.awt.Dimension(21, 13));
        jLabel52.setMinimumSize(new java.awt.Dimension(21, 13));
        jLabel52.setPreferredSize(new java.awt.Dimension(21, 13));

        jLabel54.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel54.setText("EAN");
        jLabel54.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jTextField_EtqSimple_lan.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        jLabel55.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel55.setText("IDIOMA");
        jLabel55.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
        jPanel41.setLayout(jPanel41Layout);
        jPanel41Layout.setHorizontalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel41Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel41Layout.createSequentialGroup()
                        .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel54, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                            .addComponent(jLabel52, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel48, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(9, 9, 9))
                    .addGroup(jPanel41Layout.createSequentialGroup()
                        .addComponent(jLabel55, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jText_EtqSimple_descrip)
                    .addGroup(jPanel41Layout.createSequentialGroup()
                        .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jText_EtqSimple_Ean, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jText_EtqSimple_ref, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_EtqSimple_lan, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel41Layout.setVerticalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel41Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_EtqSimple_ref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_EtqSimple_descrip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel54)
                    .addComponent(jText_EtqSimple_Ean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_EtqSimple_lan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel55))
                .addGap(6, 6, 6))
        );

        jPanel42.setBackground(new java.awt.Color(141, 191, 72));
        jPanel42.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButton_EtqSimple_Imprimir.setText("Imprimir");
        jButton_EtqSimple_Imprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EtqSimple_ImprimirActionPerformed(evt);
            }
        });

        jButton_EtqSimple_Generar.setText("Generar");
        jButton_EtqSimple_Generar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EtqSimple_GenerarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
        jPanel42.setLayout(jPanel42Layout);
        jPanel42Layout.setHorizontalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_EtqSimple_Generar)
                .addGap(18, 18, 18)
                .addComponent(jButton_EtqSimple_Imprimir)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel42Layout.setVerticalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addContainerGap(48, Short.MAX_VALUE)
                .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_EtqSimple_Generar)
                    .addComponent(jButton_EtqSimple_Imprimir))
                .addGap(45, 45, 45))
        );

        jPanel43.setBackground(new java.awt.Color(141, 191, 72));
        jPanel43.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jCombo_EtqSimple_ref.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "REFERENCIA" }));
        jCombo_EtqSimple_ref.setToolTipText("Despliega para ver referencia + cliente");
        jCombo_EtqSimple_ref.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jCombo_EtqSimple_ref.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCombo_EtqSimple_refItemStateChanged(evt);
            }
        });
        jCombo_EtqSimple_ref.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCombo_EtqSimple_refMouseClicked(evt);
            }
        });
        jCombo_EtqSimple_ref.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCombo_EtqSimple_refActionPerformed(evt);
            }
        });

        jLabel56.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel56.setText("Núm. etiquetas");

        jFormattedTextField_CantRef.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_CantRef.setText("1");
        jFormattedTextField_CantRef.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_CantRef.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_CantRefFocusLost(evt);
            }
        });
        jFormattedTextField_CantRef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CantRefActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel43Layout = new javax.swing.GroupLayout(jPanel43);
        jPanel43.setLayout(jPanel43Layout);
        jPanel43Layout.setHorizontalGroup(
            jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel43Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel56, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFormattedTextField_CantRef, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 298, Short.MAX_VALUE)
                .addComponent(jCombo_EtqSimple_ref, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel43Layout.setVerticalGroup(
            jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel43Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCombo_EtqSimple_ref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField_CantRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel56)))
        );

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel39Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel39Layout.createSequentialGroup()
                        .addComponent(jPanel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel39Layout.setVerticalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel39Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel41, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel42, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jPanel40.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel40Layout.setVerticalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLayeredPane7.setBackground(new java.awt.Color(141, 191, 72));

        jTextArea8.setEditable(false);
        jTextArea8.setColumns(20);
        jTextArea8.setRows(5);
        jTextArea8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jTextArea8.setEnabled(false);
        jScrollPane11.setViewportView(jTextArea8);

        jLabel_logo6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        javax.swing.GroupLayout jLayeredPane7Layout = new javax.swing.GroupLayout(jLayeredPane7);
        jLayeredPane7.setLayout(jLayeredPane7Layout);
        jLayeredPane7Layout.setHorizontalGroup(
            jLayeredPane7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane7Layout.createSequentialGroup()
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_logo6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jLayeredPane7Layout.setVerticalGroup(
            jLayeredPane7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel_logo6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jLayeredPane7Layout.createSequentialGroup()
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jLayeredPane7.setLayer(jScrollPane11, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane7.setLayer(jLabel_logo6, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
        jPanel38.setLayout(jPanel38Layout);
        jPanel38Layout.setHorizontalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel38Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLayeredPane7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel38Layout.setVerticalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel38Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLayeredPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jDialog7Layout = new javax.swing.GroupLayout(jDialog7.getContentPane());
        jDialog7.getContentPane().setLayout(jDialog7Layout);
        jDialog7Layout.setHorizontalGroup(
            jDialog7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialog7Layout.setVerticalGroup(
            jDialog7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jDialog8.setTitle("Referencias");
        jDialog8.setBackground(new java.awt.Color(240, 181, 81));
        jDialog8.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog8.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog8.setForeground(java.awt.Color.gray);
        jDialog8.setLocation(new java.awt.Point(0, 0));
        jDialog8.setResizable(false);
        jDialog8.setSize(new java.awt.Dimension(790, 620));
        jDialog8.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog8WindowClosed(evt);
            }
        });

        jPanel44.setBackground(new java.awt.Color(240, 232, 150));

        jPanel45.setBackground(new java.awt.Color(141, 191, 72));

        jPanel46.setBackground(new java.awt.Color(141, 191, 72));
        jPanel46.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jText_ElCorte_RefCte.setEditable(false);
        jText_ElCorte_RefCte.setEnabled(false);

        jText_ElCorte_Descrip.setEditable(false);
        jText_ElCorte_Descrip.setEnabled(false);

        jLabel53.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel53.setText("Descripción");
        jLabel53.setMaximumSize(new java.awt.Dimension(21, 13));
        jLabel53.setMinimumSize(new java.awt.Dimension(21, 13));
        jLabel53.setPreferredSize(new java.awt.Dimension(21, 13));

        jLabel57.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel57.setText("Ref. Cte.");
        jLabel57.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jTextField_ElCorte_Color.setEditable(false);
        jTextField_ElCorte_Color.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jTextField_ElCorte_Color.setEnabled(false);

        jLabel58.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel58.setText("Color");
        jLabel58.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jTextField_ElCorte_UdCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_ElCorte_UdCajaActionPerformed(evt);
            }
        });
        jTextField_ElCorte_UdCaja.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTextField_ElCorte_UdCajaPropertyChange(evt);
            }
        });

        jLabel62.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel62.setText("Uds / Caja");

        jTextField_ElCorte_Sucursal.setEditable(false);
        jTextField_ElCorte_Sucursal.setEnabled(false);

        jLabel63.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel63.setText("Sucursal");

        jTextField_ElCorte_Depart.setEditable(false);
        jTextField_ElCorte_Depart.setEnabled(false);

        jLabel64.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel64.setText("Depart.");

        jTextField_ElCorte_Ped.setEditable(false);
        jTextField_ElCorte_Ped.setEnabled(false);

        jLabel65.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel65.setText("Nº Ped.");

        jTextField_ElCorte_Alb2.setEditable(false);
        jTextField_ElCorte_Alb2.setEnabled(false);

        jLabel66.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel66.setText("Albarán:");

        jLabel67.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel67.setForeground(new java.awt.Color(255, 51, 51));
        jLabel67.setText("(Rellena Uds y presiona ENTER)");

        javax.swing.GroupLayout jPanel46Layout = new javax.swing.GroupLayout(jPanel46);
        jPanel46.setLayout(jPanel46Layout);
        jPanel46Layout.setHorizontalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel46Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel57, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                                    .addComponent(jLabel53, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(9, 9, 9))
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel58, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                                    .addComponent(jLabel63)
                                    .addComponent(jLabel65))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jText_ElCorte_Descrip)
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addComponent(jTextField_ElCorte_Color, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53)
                                .addComponent(jLabel62, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField_ElCorte_UdCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addComponent(jTextField_ElCorte_Ped, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel46Layout.createSequentialGroup()
                        .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel46Layout.createSequentialGroup()
                                .addGap(111, 111, 111)
                                .addComponent(jTextField_ElCorte_Sucursal, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53)
                                .addComponent(jLabel64)
                                .addGap(41, 41, 41))
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel46Layout.createSequentialGroup()
                                        .addGap(111, 111, 111)
                                        .addComponent(jText_ElCorte_RefCte, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel46Layout.createSequentialGroup()
                                        .addGap(24, 24, 24)
                                        .addComponent(jLabel66)
                                        .addGap(44, 44, 44)
                                        .addComponent(jTextField_ElCorte_Alb2, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(jLabel67, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addComponent(jTextField_ElCorte_Depart, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel46Layout.setVerticalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel46Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_ElCorte_Sucursal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel63)
                    .addComponent(jTextField_ElCorte_Depart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel64))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_ElCorte_Ped, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel65))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_ElCorte_Alb2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel66))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_ElCorte_Descrip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel57)
                    .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jText_ElCorte_RefCte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel67)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_ElCorte_Color, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel58)
                    .addComponent(jTextField_ElCorte_UdCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel62))
                .addGap(6, 6, 6))
        );

        jPanel47.setBackground(new java.awt.Color(141, 191, 72));
        jPanel47.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel47.setName(""); // NOI18N

        jButton_ElCorte_Print.setText("Imprimir");
        jButton_ElCorte_Print.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ElCorte_PrintActionPerformed(evt);
            }
        });

        jButton_ElCorte_Generar.setText("Consultar");
        jButton_ElCorte_Generar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ElCorte_GenerarActionPerformed(evt);
            }
        });

        jLabel49.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel49.setText("Albarán");
        jLabel49.setMaximumSize(new java.awt.Dimension(21, 13));
        jLabel49.setMinimumSize(new java.awt.Dimension(21, 13));
        jLabel49.setPreferredSize(new java.awt.Dimension(21, 13));

        jText_ElCorte_Alb.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jText_ElCorte_Alb.setToolTipText("Introduce referencia para hacer consulta");

        jTextField_ElCorte_Art.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextField_ElCorte_Art.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_ElCorte_ArtActionPerformed(evt);
            }
        });

        jLabel61.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel61.setText("Art.");

        javax.swing.GroupLayout jPanel47Layout = new javax.swing.GroupLayout(jPanel47);
        jPanel47.setLayout(jPanel47Layout);
        jPanel47Layout.setHorizontalGroup(
            jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel47Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel47Layout.createSequentialGroup()
                        .addComponent(jButton_ElCorte_Generar)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_ElCorte_Print)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel49, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel47Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jText_ElCorte_Alb, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel47Layout.createSequentialGroup()
                        .addComponent(jLabel61)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField_ElCorte_Art, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel47Layout.setVerticalGroup(
            jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel47Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jText_ElCorte_Alb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_ElCorte_Art, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel61))
                .addGap(18, 18, 18)
                .addGroup(jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_ElCorte_Generar)
                    .addComponent(jButton_ElCorte_Print))
                .addGap(45, 45, 45))
        );

        jPanel48.setBackground(new java.awt.Color(141, 191, 72));
        jPanel48.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jCombo_ElCorte.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ALBARAN" }));
        jCombo_ElCorte.setToolTipText("Despliega para ver Albarán + Art.");
        jCombo_ElCorte.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jCombo_ElCorte.setEnabled(false);
        jCombo_ElCorte.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCombo_ElCorteItemStateChanged(evt);
            }
        });
        jCombo_ElCorte.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCombo_ElCorteMouseClicked(evt);
            }
        });
        jCombo_ElCorte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCombo_ElCorteActionPerformed(evt);
            }
        });

        jLabel59.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel59.setText("Núm. etiquetas");

        jFormattedTextField_CantElCorte.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_CantElCorte.setText("1");
        jFormattedTextField_CantElCorte.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_CantElCorte.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_CantElCorteFocusLost(evt);
            }
        });
        jFormattedTextField_CantElCorte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_CantElCorteActionPerformed(evt);
            }
        });

        jTextField_ElCorte_Cte.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextField_ElCorte_Cte.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField_ElCorte_Cte.setText("600016");

        jLabel60.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel60.setText("Grupo");

        javax.swing.GroupLayout jPanel48Layout = new javax.swing.GroupLayout(jPanel48);
        jPanel48.setLayout(jPanel48Layout);
        jPanel48Layout.setHorizontalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel59, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFormattedTextField_CantElCorte, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField_ElCorte_Cte, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(jCombo_ElCorte, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel48Layout.setVerticalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel48Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCombo_ElCorte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField_CantElCorte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel59)
                    .addComponent(jTextField_ElCorte_Cte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel60)))
        );

        javax.swing.GroupLayout jPanel45Layout = new javax.swing.GroupLayout(jPanel45);
        jPanel45.setLayout(jPanel45Layout);
        jPanel45Layout.setHorizontalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel45Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel45Layout.createSequentialGroup()
                        .addComponent(jPanel46, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel47, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel45Layout.setVerticalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel45Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel46, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jPanel47, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel49.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jPanel49Layout = new javax.swing.GroupLayout(jPanel49);
        jPanel49.setLayout(jPanel49Layout);
        jPanel49Layout.setHorizontalGroup(
            jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel49Layout.setVerticalGroup(
            jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLayeredPane8.setBackground(new java.awt.Color(141, 191, 72));

        jTextArea9.setEditable(false);
        jTextArea9.setColumns(20);
        jTextArea9.setRows(5);
        jTextArea9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jTextArea9.setEnabled(false);
        jScrollPane12.setViewportView(jTextArea9);

        jLabel_logo7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        javax.swing.GroupLayout jLayeredPane8Layout = new javax.swing.GroupLayout(jLayeredPane8);
        jLayeredPane8.setLayout(jLayeredPane8Layout);
        jLayeredPane8Layout.setHorizontalGroup(
            jLayeredPane8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane8Layout.createSequentialGroup()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_logo7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jLayeredPane8Layout.setVerticalGroup(
            jLayeredPane8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel_logo7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jLayeredPane8Layout.createSequentialGroup()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jLayeredPane8.setLayer(jScrollPane12, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane8.setLayer(jLabel_logo7, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel44Layout = new javax.swing.GroupLayout(jPanel44);
        jPanel44.setLayout(jPanel44Layout);
        jPanel44Layout.setHorizontalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel44Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLayeredPane8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel49, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel45, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel44Layout.setVerticalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel44Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLayeredPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jDialog8Layout = new javax.swing.GroupLayout(jDialog8.getContentPane());
        jDialog8.getContentPane().setLayout(jDialog8Layout);
        jDialog8Layout.setHorizontalGroup(
            jDialog8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialog8Layout.setVerticalGroup(
            jDialog8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jDialog9.setTitle("Stocks");
        jDialog9.setBackground(new java.awt.Color(240, 181, 81));
        jDialog9.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog9.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog9.setForeground(java.awt.Color.gray);
        jDialog9.setLocation(new java.awt.Point(0, 0));
        jDialog9.setSize(new java.awt.Dimension(790, 620));
        jDialog9.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog9WindowClosed(evt);
            }
        });

        jPanel50.setBackground(new java.awt.Color(240, 232, 150));

        jPanel51.setBackground(new java.awt.Color(141, 191, 72));

        jButton9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton9.setText("Imprimir");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jPanel52.setBackground(new java.awt.Color(141, 191, 72));

        jFormattedTextField_fontSize4.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_fontSize4.setText("128");
        jFormattedTextField_fontSize4.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_fontSize4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_fontSize4FocusLost(evt);
            }
        });
        jFormattedTextField_fontSize4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_fontSize4ActionPerformed(evt);
            }
        });
        jFormattedTextField_fontSize4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jFormattedTextField_fontSize4KeyTyped(evt);
            }
        });

        jLabel68.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel68.setText("Tamaño fuente");

        javax.swing.GroupLayout jPanel52Layout = new javax.swing.GroupLayout(jPanel52);
        jPanel52.setLayout(jPanel52Layout);
        jPanel52Layout.setHorizontalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel52Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel68, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFormattedTextField_fontSize4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel52Layout.setVerticalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel52Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_fontSize4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel68))
                .addGap(0, 79, Short.MAX_VALUE))
        );

        buttonGroup_stocks.add(jRadioButton_PlantaF);
        jRadioButton_PlantaF.setText("Famesa");
        jRadioButton_PlantaF.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_PlantaFItemStateChanged(evt);
            }
        });

        buttonGroup_stocks.add(jRadioButton_PlantaT);
        jRadioButton_PlantaT.setText("Trilla");
        jRadioButton_PlantaT.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_PlantaTItemStateChanged(evt);
            }
        });

        jComboBox_Stock.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Lotes" }));
        jComboBox_Stock.setToolTipText("");
        jComboBox_Stock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jComboBox_StockMouseClicked(evt);
            }
        });

        jButton_ItmLot.setText("Consulta");
        jButton_ItmLot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ItmLotActionPerformed(evt);
            }
        });

        jTextStock_arti.setText("ARTI");

        jTextStock_lote.setText("LOTE");

        javax.swing.GroupLayout jPanel53Layout = new javax.swing.GroupLayout(jPanel53);
        jPanel53.setLayout(jPanel53Layout);
        jPanel53Layout.setHorizontalGroup(
            jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel53Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton_ItmLot)
                    .addGroup(jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jRadioButton_PlantaF, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jRadioButton_PlantaT, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jComboBox_Stock, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextStock_arti, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextStock_lote, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel53Layout.setVerticalGroup(
            jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel53Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel53Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jTextStock_arti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextStock_lote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel53Layout.createSequentialGroup()
                        .addComponent(jRadioButton_PlantaF)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton_PlantaT, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox_Stock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(jButton_ItmLot)))
                .addContainerGap())
        );

        jText_StockHasta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jText_StockHastaActionPerformed(evt);
            }
        });

        jLabel69.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel69.setText("Sublote hasta:");

        jLabel70.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel70.setText("Sublote desde:");

        jFormattedTextField_cantidadUbi1.setEditable(false);
        jFormattedTextField_cantidadUbi1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField_cantidadUbi1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_cantidadUbi1.setText("1");
        jFormattedTextField_cantidadUbi1.setToolTipText("Introduce el número de etiquetas a imprimir");
        jFormattedTextField_cantidadUbi1.setEnabled(false);
        jFormattedTextField_cantidadUbi1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jFormattedTextField_cantidadUbi1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFormattedTextField_cantidadUbi1FocusLost(evt);
            }
        });
        jFormattedTextField_cantidadUbi1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_cantidadUbi1ActionPerformed(evt);
            }
        });

        jLabel71.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel71.setText("Núm. etiquetas");

        javax.swing.GroupLayout jPanel54Layout = new javax.swing.GroupLayout(jPanel54);
        jPanel54.setLayout(jPanel54Layout);
        jPanel54Layout.setHorizontalGroup(
            jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel54Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel54Layout.createSequentialGroup()
                        .addGroup(jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel70, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel69, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jText_StockHasta, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jText_StockDesde, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel54Layout.createSequentialGroup()
                        .addComponent(jLabel71, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jFormattedTextField_cantidadUbi1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel54Layout.setVerticalGroup(
            jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel54Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_StockDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel70))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_StockHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel69))
                .addGap(18, 18, 18)
                .addGroup(jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextField_cantidadUbi1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel71))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel51Layout = new javax.swing.GroupLayout(jPanel51);
        jPanel51.setLayout(jPanel51Layout);
        jPanel51Layout.setHorizontalGroup(
            jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel51Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel51Layout.createSequentialGroup()
                        .addComponent(jButton9)
                        .addGap(152, 152, 152))
                    .addGroup(jPanel51Layout.createSequentialGroup()
                        .addComponent(jPanel53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel54, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(jPanel52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(156, Short.MAX_VALUE))
        );
        jPanel51Layout.setVerticalGroup(
            jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel51Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel51Layout.createSequentialGroup()
                        .addComponent(jPanel52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel51Layout.createSequentialGroup()
                        .addGroup(jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel54, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton9)))
                .addContainerGap())
        );

        jPanel55.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jPanel55Layout = new javax.swing.GroupLayout(jPanel55);
        jPanel55.setLayout(jPanel55Layout);
        jPanel55Layout.setHorizontalGroup(
            jPanel55Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel55Layout.setVerticalGroup(
            jPanel55Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLayeredPane9.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jLayeredPane9Layout = new javax.swing.GroupLayout(jLayeredPane9);
        jLayeredPane9.setLayout(jLayeredPane9Layout);
        jLayeredPane9Layout.setHorizontalGroup(
            jLayeredPane9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jLayeredPane9Layout.setVerticalGroup(
            jLayeredPane9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );

        jLabel_logo8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        jTextArea10.setColumns(20);
        jTextArea10.setRows(5);
        jTextArea10.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jTextArea10.setEnabled(false);
        jScrollPane13.setViewportView(jTextArea10);

        javax.swing.GroupLayout jPanel50Layout = new javax.swing.GroupLayout(jPanel50);
        jPanel50.setLayout(jPanel50Layout);
        jPanel50Layout.setHorizontalGroup(
            jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel50Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel50Layout.createSequentialGroup()
                        .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel_logo8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLayeredPane9))
                    .addComponent(jPanel55, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel51, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel50Layout.setVerticalGroup(
            jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel50Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel51, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel50Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLayeredPane9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel50Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_logo8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel55, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jDialog9Layout = new javax.swing.GroupLayout(jDialog9.getContentPane());
        jDialog9.getContentPane().setLayout(jDialog9Layout);
        jDialog9Layout.setHorizontalGroup(
            jDialog9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialog9Layout.setVerticalGroup(
            jDialog9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jDialog10.setTitle("Etiquetas Dascher/XPO/MRW");
        jDialog10.setBackground(new java.awt.Color(204, 255, 51));
        jDialog10.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog10.setLocation(new java.awt.Point(0, 0));
        jDialog10.setPreferredSize(new java.awt.Dimension(900, 575));
        jDialog10.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog10WindowClosed(evt);
            }
        });

        jPanel56.setBackground(new java.awt.Color(240, 232, 150));

        jPanel57.setBackground(new java.awt.Color(141, 191, 72));

        jText_alb_dascher.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jText_alb_dascher.setToolTipText("Introduce el pedido y pulsa \"ENTER\"");
        jText_alb_dascher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jText_alb_dascherActionPerformed(evt);
            }
        });
        jText_alb_dascher.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jText_alb_dascherKeyTyped(evt);
            }
        });

        jLabel72.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel72.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel72.setText("Alb:");

        jButton10.setText("Imprimir");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jTb_dascher.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Cod_Remitente", "NumAlb", "Nom_Consig", "Dir_Consig", "CP_Consig", "Pob_Consig", "Pais_Consig", "Bultos", "Peso", "Portes", "Mail_Consig", "Tfno_Consig", "Seleccionado", "fecha_carga", "Fecha_entrega", "Obs1", "Obs2", "Dir2", "Obs_etiqueta", "Volumen", "Ped_Cliente", "Bultos2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, true, false, true, true, true, true, true, true, false, true, true, true, true, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane14.setViewportView(jTb_dascher);
        if (jTb_dascher.getColumnModel().getColumnCount() > 0) {
            jTb_dascher.getColumnModel().getColumn(19).setResizable(false);
            jTb_dascher.getColumnModel().getColumn(20).setResizable(false);
        }

        jText_empresa1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jText_empresa1.setText("FABRICANTES DE MENAJE, S.A");
        jText_empresa1.setToolTipText("Nombre de empresa a imprimir");
        jText_empresa1.setEnabled(false);

        jLabel74.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel74.setText("Empresa");

        jLabel75.setText("Teclea albarán y presiona ENTER. Pon % para ver todos");

        buttonGroup_tipo_transportista.add(jRadioButton_DASCHER);
        jRadioButton_DASCHER.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jRadioButton_DASCHER.setSelected(true);
        jRadioButton_DASCHER.setText("DASCHER");
        jRadioButton_DASCHER.setBorderPainted(true);

        buttonGroup_tipo_transportista.add(jRadioButton_XPO);
        jRadioButton_XPO.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jRadioButton_XPO.setText("XPO");
        jRadioButton_XPO.setBorderPainted(true);
        jRadioButton_XPO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_XPOActionPerformed(evt);
            }
        });

        buttonGroup_tipo_transportista.add(jRadioButton_MRW);
        jRadioButton_MRW.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jRadioButton_MRW.setText("MRW");
        jRadioButton_MRW.setBorderPainted(true);

        jLabel85.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel85.setText("Selecciona el trasportista->");

        javax.swing.GroupLayout jPanel57Layout = new javax.swing.GroupLayout(jPanel57);
        jPanel57.setLayout(jPanel57Layout);
        jPanel57Layout.setHorizontalGroup(
            jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel57Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel57Layout.createSequentialGroup()
                        .addComponent(jScrollPane14)
                        .addContainerGap())
                    .addGroup(jPanel57Layout.createSequentialGroup()
                        .addComponent(jLabel72, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jText_alb_dascher, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel57Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 165, Short.MAX_VALUE)
                                .addComponent(jLabel74, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jText_empresa1, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(21, 21, 21))
                            .addGroup(jPanel57Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel75, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel57Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel85, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton_DASCHER)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton_XPO)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton_MRW, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel57Layout.setVerticalGroup(
            jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel57Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel72)
                    .addComponent(jText_alb_dascher, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel75))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_empresa1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel74))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel57Layout.createSequentialGroup()
                        .addComponent(jButton10)
                        .addGap(1, 1, 1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel57Layout.createSequentialGroup()
                        .addGroup(jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton_MRW)
                            .addGroup(jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jRadioButton_DASCHER)
                                .addComponent(jRadioButton_XPO)
                                .addComponent(jLabel85)))
                        .addContainerGap())))
        );

        jLayeredPane10.setBackground(new java.awt.Color(141, 191, 72));

        jTextArea11.setColumns(20);
        jTextArea11.setRows(5);
        jTextArea11.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jScrollPane15.setViewportView(jTextArea11);
        jTextArea11.getAccessibleContext().setAccessibleName("Fichero para Dascher");

        jLabel_logo9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        jPanel58.setBackground(new java.awt.Color(141, 191, 72));

        javax.swing.GroupLayout jPanel58Layout = new javax.swing.GroupLayout(jPanel58);
        jPanel58.setLayout(jPanel58Layout);
        jPanel58Layout.setHorizontalGroup(
            jPanel58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel58Layout.setVerticalGroup(
            jPanel58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jTextArea_xpo.setColumns(20);
        jTextArea_xpo.setRows(5);
        jScrollPane16.setViewportView(jTextArea_xpo);

        javax.swing.GroupLayout jLayeredPane10Layout = new javax.swing.GroupLayout(jLayeredPane10);
        jLayeredPane10.setLayout(jLayeredPane10Layout);
        jLayeredPane10Layout.setHorizontalGroup(
            jLayeredPane10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane10Layout.createSequentialGroup()
                .addGroup(jLayeredPane10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel58, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jLayeredPane10Layout.createSequentialGroup()
                        .addComponent(jScrollPane15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel_logo9, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)))
                .addContainerGap())
        );
        jLayeredPane10Layout.setVerticalGroup(
            jLayeredPane10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_logo9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jLayeredPane10Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel58, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(148, 148, 148))
        );
        jLayeredPane10.setLayer(jScrollPane15, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane10.setLayer(jLabel_logo9, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane10.setLayer(jPanel58, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane10.setLayer(jScrollPane16, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel56Layout = new javax.swing.GroupLayout(jPanel56);
        jPanel56.setLayout(jPanel56Layout);
        jPanel56Layout.setHorizontalGroup(
            jPanel56Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel56Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel56Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel57, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLayeredPane10))
                .addContainerGap())
        );
        jPanel56Layout.setVerticalGroup(
            jPanel56Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel56Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel57, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jDialog10Layout = new javax.swing.GroupLayout(jDialog10.getContentPane());
        jDialog10.getContentPane().setLayout(jDialog10Layout);
        jDialog10Layout.setHorizontalGroup(
            jDialog10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog10Layout.createSequentialGroup()
                .addComponent(jPanel56, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialog10Layout.setVerticalGroup(
            jDialog10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog10Layout.createSequentialGroup()
                .addComponent(jPanel56, javax.swing.GroupLayout.PREFERRED_SIZE, 564, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        jDialog10.getAccessibleContext().setAccessibleName("albaranes dascher");

        jDialog11.setTitle("Impresión de etiquetas SSCC");
        jDialog11.setBackground(new java.awt.Color(255, 153, 51));
        jDialog11.setBounds(new java.awt.Rectangle(550, 600, 815, 600));
        jDialog11.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog11.setLocation(new java.awt.Point(0, 0));

        jPanel59.setBackground(new java.awt.Color(240, 232, 150));
        jPanel59.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel59.setEnabled(false);

        jPanel60.setBackground(new java.awt.Color(141, 191, 72));

        jTextField_AlbCte1.setEditable(false);
        jTextField_AlbCte1.setEnabled(false);

        jComboBox_AlbSSCC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ALB DENOX" }));
        jComboBox_AlbSSCC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_AlbSSCCActionPerformed(evt);
            }
        });

        jTextField_AlbPedidoCte1.setEditable(false);
        jTextField_AlbPedidoCte1.setEnabled(false);

        jTextField_AlbTrans1.setEditable(false);
        jTextField_AlbTrans1.setEnabled(false);

        jTextField_AlbDirEnv1.setEditable(false);
        jTextField_AlbDirEnv1.setEnabled(false);

        jTextField_AlbBultos1.setEditable(false);
        jTextField_AlbBultos1.setEnabled(false);
        jTextField_AlbBultos1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_AlbBultos1ActionPerformed(evt);
            }
        });

        jLabel73.setText("Núm. Albarán:");

        jLabel76.setText("Cód. Cliente:");

        jLabel77.setText("Pedido Cliente:");

        jLabel78.setText("Transportista:");

        jLabel79.setText("Dir. Envío:");

        jLabel80.setText("Núm. Bultos:");

        jButton_ConsultaAlbNormal1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_ConsultaAlbNormal1.setText("Consulta");
        jButton_ConsultaAlbNormal1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ConsultaAlbNormal1ActionPerformed(evt);
            }
        });

        jTextField_AlbNomCte1.setEditable(false);
        jTextField_AlbNomCte1.setEnabled(false);

        jLabel81.setText("Nombre Cliente:");

        jButton11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton11.setText("IMPRIMIR");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jTextField_AlbDir3.setEnabled(false);

        jTextField_AlbDirPostal1.setEnabled(false);

        jButton_RefrescarAlb2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_RefrescarAlb2.setText("REFRESCAR");
        jButton_RefrescarAlb2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RefrescarAlb2ActionPerformed(evt);
            }
        });

        jTextField_AlbPrep1.setEnabled(false);
        jTextField_AlbPrep1.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel82.setText("Preparador:");

        buttonGroup_SSCC.add(jRadioButton_alb_fam1);
        jRadioButton_alb_fam1.setSelected(true);
        jRadioButton_alb_fam1.setText("FAMESA");
        jRadioButton_alb_fam1.setActionCommand("FAM");
        jRadioButton_alb_fam1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton_alb_fam1ItemStateChanged(evt);
            }
        });

        buttonGroup_SSCC.add(jRadioButton_alb_tri1);
        jRadioButton_alb_tri1.setText("TRILLA");
        jRadioButton_alb_tri1.setActionCommand("TRI");
        jRadioButton_alb_tri1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_alb_tri1ActionPerformed(evt);
            }
        });

        jTextField_SSCC_pallets.setEditable(false);
        jTextField_SSCC_pallets.setEnabled(false);
        jTextField_SSCC_pallets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_SSCC_palletsActionPerformed(evt);
            }
        });

        jLabel84.setText("Núm. Pallets:");

        javax.swing.GroupLayout jPanel60Layout = new javax.swing.GroupLayout(jPanel60);
        jPanel60.setLayout(jPanel60Layout);
        jPanel60Layout.setHorizontalGroup(
            jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel60Layout.createSequentialGroup()
                .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel60Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel60Layout.createSequentialGroup()
                                .addComponent(jLabel73, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(7, 7, 7))
                            .addGroup(jPanel60Layout.createSequentialGroup()
                                .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel80, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel78, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                                    .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel76, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel77, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
                                .addGap(18, 18, Short.MAX_VALUE)))
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_AlbPedidoCte1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_AlbCte1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel60Layout.createSequentialGroup()
                                .addComponent(jTextField_AlbNumAlb1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jButton_ConsultaAlbNormal1))
                            .addGroup(jPanel60Layout.createSequentialGroup()
                                .addComponent(jTextField_AlbBultos1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel84)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_SSCC_pallets, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTextField_AlbTrans1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel60Layout.createSequentialGroup()
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton_RefrescarAlb2, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(150, 150, 150))
                            .addComponent(jTextField_AlbDir3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel60Layout.createSequentialGroup()
                                .addComponent(jLabel79, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_AlbDirEnv1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel60Layout.createSequentialGroup()
                                .addComponent(jLabel82)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField_AlbPrep1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField_AlbDirPostal1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel60Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel60Layout.createSequentialGroup()
                                .addComponent(jRadioButton_alb_fam1, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButton_alb_tri1, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel60Layout.createSequentialGroup()
                                .addComponent(jComboBox_AlbSSCC, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel81)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_AlbNomCte1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel60Layout.setVerticalGroup(
            jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel60Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton_alb_fam1)
                    .addComponent(jRadioButton_alb_tri1))
                .addGap(11, 11, 11)
                .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel60Layout.createSequentialGroup()
                        .addComponent(jComboBox_AlbSSCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbNumAlb1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel73)
                            .addComponent(jButton_ConsultaAlbNormal1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbCte1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel76))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbPedidoCte1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel77))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbTrans1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel78)))
                    .addGroup(jPanel60Layout.createSequentialGroup()
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbNomCte1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel81))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbDirEnv1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel79))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_AlbDir3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_AlbDirPostal1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_AlbPrep1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel82))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel80)
                    .addComponent(jTextField_AlbBultos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton11)
                    .addComponent(jButton_RefrescarAlb2)
                    .addComponent(jTextField_SSCC_pallets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel84))
                .addGap(0, 9, Short.MAX_VALUE))
        );

        jTextArea12.setColumns(20);
        jTextArea12.setRows(5);
        jTextArea12.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jTextArea12.setEnabled(false);
        jTextArea12.setName(""); // NOI18N
        jScrollPane17.setViewportView(jTextArea12);

        jLabel83.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        javax.swing.GroupLayout jLayeredPane11Layout = new javax.swing.GroupLayout(jLayeredPane11);
        jLayeredPane11.setLayout(jLayeredPane11Layout);
        jLayeredPane11Layout.setHorizontalGroup(
            jLayeredPane11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel83, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jLayeredPane11Layout.setVerticalGroup(
            jLayeredPane11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane11Layout.createSequentialGroup()
                .addGroup(jLayeredPane11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane11Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(jLabel83, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jLayeredPane11Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        jLayeredPane11.setLayer(jScrollPane17, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane11.setLayer(jLabel83, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel59Layout = new javax.swing.GroupLayout(jPanel59);
        jPanel59.setLayout(jPanel59Layout);
        jPanel59Layout.setHorizontalGroup(
            jPanel59Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel59Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel59Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel60, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLayeredPane11))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel59Layout.setVerticalGroup(
            jPanel59Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel59Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel60, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout jDialog11Layout = new javax.swing.GroupLayout(jDialog11.getContentPane());
        jDialog11.getContentPane().setLayout(jDialog11Layout);
        jDialog11Layout.setHorizontalGroup(
            jDialog11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel59, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jDialog11Layout.setVerticalGroup(
            jDialog11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel59, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Generación de etiquetas V79");
        setBackground(new java.awt.Color(141, 191, 72));

        jPanel7.setBackground(new java.awt.Color(240, 232, 150));

        jPanel1.setBackground(new java.awt.Color(141, 191, 72));

        jText_albaran.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N

        jLabel_albaran.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel_albaran.setText("Albarán:");

        jButton_consultar.setText("Consultar");
        jButton_consultar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_consultarActionPerformed(evt);
            }
        });

        buttonGroup_idioma_alb.add(jRadioButton4);
        jRadioButton4.setSelected(true);
        jRadioButton4.setText("SPA");
        jRadioButton4.setName("SPA"); // NOI18N

        buttonGroup_idioma_alb.add(jRadioButton5);
        jRadioButton5.setText("ENG");
        jRadioButton5.setName("ENG"); // NOI18N

        buttonGroup_idioma_alb.add(jRadioButton6);
        jRadioButton6.setText("FRA");
        jRadioButton6.setName("FRA"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel_albaran, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jText_albaran, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jRadioButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                .addComponent(jButton_consultar, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(180, 180, 180))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_albaran, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_albaran)
                    .addComponent(jButton_consultar)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton5)
                    .addComponent(jRadioButton6))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(141, 191, 72));

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(240, 240, 240));
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jScrollPane1.setViewportView(jTextArea1);

        jButton2.setText("Imprimir");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pkg_expediciones/logo_denox.png"))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jTextArea_log.setBackground(new java.awt.Color(255, 153, 51));
        jTextArea_log.setColumns(20);
        jTextArea_log.setForeground(new java.awt.Color(255, 0, 51));
        jTextArea_log.setLineWrap(true);
        jTextArea_log.setRows(5);
        jTextArea_log.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Avisos", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jTextArea_log.setEnabled(false);
        jScrollPane5.setViewportView(jTextArea_log);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 583, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jButton2))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenu_ir.setText("Menú");
        jMenu_ir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_irActionPerformed(evt);
            }
        });

        jMenuItem_albaranes.setText("Albaranes (Cód. Barras)");
        jMenuItem_albaranes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_albaranesActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem_albaranes);

        jMenuItem_albaranNormal.setText("Imp. Etiquetas Albarán (simple)");
        jMenuItem_albaranNormal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_albaranNormalActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem_albaranNormal);

        jMenuItem_referencias.setText("Referencias");
        jMenuItem_referencias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_referenciasActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem_referencias);

        jMenuItem1.setText("Pedidos Proveedor");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem1);
        jMenu_ir.add(jSeparator1);

        jMenuItem_tonta.setText("Etiqueta Tonta");
        jMenuItem_tonta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_tontaActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem_tonta);

        jMenuItem_OF.setText("Etiquetas OF");
        jMenuItem_OF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OFActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem_OF);

        jMenuItem3.setText("Etiquetas Ubicaciones");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem3);

        jMenuItem4.setText("Etiqueta Ref. Simple");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem4);

        jMenuItem5.setText("Etiqueta El Corte");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem5);

        jMenuItem6.setText("Etiqueta Stock");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem6);
        jMenu_ir.add(jSeparator2);

        jMenuItem_alb_dascher.setText("Etiquetas Dascher / XPO / MRW");
        jMenuItem_alb_dascher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_alb_dascherActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem_alb_dascher);

        jMenuItem_SSCC.setText("Etiquetas SSCC");
        jMenuItem_SSCC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SSCCActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem_SSCC);

        jMenuItem_salir.setText("Salir");
        jMenuItem_salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_salirActionPerformed(evt);
            }
        });
        jMenu_ir.add(jMenuItem_salir);

        jMenuBar1.add(jMenu_ir);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_consultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_consultarActionPerformed
        jTextArea1.setText(null);//limpio para no tener datos anteriores
        jTextArea_log.setText(null);

        String idioma;
        if (jRadioButton4.isSelected()) {
            idioma = jRadioButton4.getName();

        } else if (jRadioButton5.isSelected()) {
            idioma = jRadioButton5.getName();
        } else {
            idioma = jRadioButton6.getName();
        }

        try {
            //String query = "SELECT ALBARAN, NUM_BULTO, ARTICULO, PEDIDO_FAM, NOM_CTE, COD_EAN  fROM V_BULTOS_ENTREGAS WHERE ALBARAN like '" + jText_albaran.getText() + "'";
            String query = "SELECT REF_SIN_COLOR, TEXTO, COD_DUN14, EAN, CAPACIDAD_DUN, CANTIDAD, FLOOR((CANTIDAD/CAPACIDAD_DUN)*2) AS ETIQUETA_POR_BULTO, NOM_CTE\n"
                    + "FROM V_EXPEDICIONES_IDIOMA_NEW\n"
                    + "WHERE ALBARAN = '" + jText_albaran.getText() + "'\n"
                    + "AND LENGUAJE = '" + idioma + "'\n"
                    + "ORDER BY REFERENCIA";
            // JOptionPane.showMessageDialog(this, query);
            rs = stmt.executeQuery(query);//rs contendrá todos los registros

            //JOptionPane.showMessageDialog(this, "bultos a generar: " + bultos + " en " + idioma);
            //zpl(1,bultos,0);
            if (rs.next() == false) {
                JOptionPane.showMessageDialog(this, "El albarán: " + jText_albaran.getText() + " no existe");
            } else {
                rs.previous();
                while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en los campos
                    if (rs.getString("COD_DUN14")==null){ //genero log cuando la referencia no tenga dun14 asignado
                        rellenaTextArea(3, "La referencia: ", rs.getString("REF_SIN_COLOR"), "No tiene DUN14", "No se imprimirá", "");
                        System.out.println("Referencia sin DUN14");
                    }
                    int bultos = rs.getInt("ETIQUETA_POR_BULTO");
                    System.out.println("bultos: " + bultos);
                    zpl(1, bultos, 0, "");
                    
                }
            }
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, "ups...  " + err.getMessage());
        }
    }//GEN-LAST:event_jButton_consultarActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //genero el fichero en c:\tmp
        generar_fichero (1)   ;
        rellenaTextArea(3, "Fichero de impresión generado", "", "", "", "");
         // manda orden de impresión al lpt1. La impresora ha de estar mapeada al LPT1
         //System.getProperty("user.dir");
        ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        
        rellenaTextArea(3, "Orden de impresión para el albarán: ", jText_albaran.getText(), "enviada", "", "");
         
        
        //a continuacion diversas pruebas que he hecho para mandar el comando 
       /*try {  
            
           String comando = "cmd /C type c:\\etiqueta.txt > lpt1";
           Process p = Runtime.getRuntime().exec(comando);  
           BufferedReader in = new BufferedReader(  
                                new InputStreamReader(p.getInputStream()));  
            String line = null;  
            while ((line = in.readLine()) != null) {  
                System.out.println(line);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }*/
        /*try{
        String rutaFichero = "C:/etiqueta.txt";
        String nombreImpresora = "LPT1";
        
        String command = String.format("CMD /C TYPE %s > %s", rutaFichero, nombreImpresora);
        Runtime.getRuntime().exec(command);
        } catch(IOException e){
            e.printStackTrace();
        }*/
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jMenuItem_salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_salirActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jMenuItem_salirActionPerformed

    private void jMenu_irActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_irActionPerformed
        // TODO add your handling code here:
          
    }//GEN-LAST:event_jMenu_irActionPerformed

    private void jMenuItem_referenciasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_referenciasActionPerformed
        //mostramos la ventana jdialog1
        
        DoDesconnect();
        DoConnect();
        jDialog1.setVisible(true);
        
        //LLenamos nuestro ComboBox
           
        try {
            
            String query = "select REFERENCIA, CLIENTE, NOM_CTE, DESCRIP, EAN FROM V_ARTI_DUN14_NEW";

            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();
            
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                
                jCombo_ref.addItem(rs.getString("REFERENCIA") + " " + rs.getString("CLIENTE") + " " + rs.getString("NOM_CTE"));

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
                //zpl();
            }
            
                

          
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
       
        }
    }//GEN-LAST:event_jMenuItem_referenciasActionPerformed
    private void consultar_referencia() {
        //ejecución de la consulta de referencia, pinto los datos en los campos correspondientes, añadido para alternativa a botón de consulta y meterlo en 
        //el evento de cambio de estado del combobox.    
        try {
//          
              //busco el idioma seleccionado parar añadir la condición a la query
            String idioma;
            if (jRadioButton1.isSelected()){
                idioma = jRadioButton1.getName();
                     
            }
            else if (jRadioButton2.isSelected()){
                idioma = jRadioButton2.getName();
            }
            else
                idioma = jRadioButton3.getName();
                    
            
            String query = "select REFERENCIA, CLIENTE, NOM_CTE, DESCRIP, EAN, TEXTO, CAPACIDAD_DUN\n"
                    + "FROM V_ARTI_DUN14_TRAD_NEW\n"
                    + "WHERE REFERENCIA = '" + jText_referencia.getText() + "'\n"
                    + "AND CLIENTE = '" + jText_ref_cliente.getText() + "'\n"
                    + "AND LENGUAJE = '" + idioma + "'";
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();
            rs.next(); //muevo el cursor al primer registro y muestro los datos en los campos
            
            if (rs.getRow()!= 0){//comprueba que haya registro en rs para evitar el error de que coja el campo descriptivo en posición 1
            jText_ref_desc_art.setText(rs.getString("TEXTO"));
            jText_ref_capacidad.setText(rs.getString("CAPACIDAD_DUN"));
            jText_ref_desc_cte.setText(rs.getString("NOM_CTE"));
            jText_ref_ean.setText(rs.getString("EAN"));
            zpl(2,Integer.parseInt(jFormattedTextField_cantidad.getText()),0,""); //le paso la cantidad de etiquetas a imprimir
            System.out.println(rs.getString("DESCRIP") + " " + rs.getString("CLIENTE") + " " + rs.getString("NOM_CTE") + " " + rs.getString("EAN"));
            }
            else
                JOptionPane.showMessageDialog(this, "no existe registro para referencia: [" + jText_referencia.getText() + "] y cliente: [" + jText_ref_cliente.getText() + ']');

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
    }
    
    private void consultar_pedpro() {
        
        try {
//           
            String query = "select PEDPRO_NUMPED, PEDPRO_PROV, PEDPRO_RAZON_SOCIAL, PEDPRO_ART, ART_SIN_COLOR, ROUND (PEDPRO_CANT_UOM) AS PEDPRO_CANT_UOM, DESCRIPCION, DUN14, CAP_DUN, COD_EAN, DESCRIP\n"
                    + "From V_PEDIDOS_PROVEEDOR_ART_NEW\n"
                    + "WHERE PEDPRO_NUMPED = '" + jText_pedproNumped.getText() +"'" ;
            
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            
                        
            inicio_tabla();//Método de inicio de jtable
                   
                               
            while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en el textArea
                if (rs.getRow() != 0) {//comprueba que haya registro en rs para evitar el error de que coja el campo descriptivo en posición 1

                    String num_ped = rs.getString("PEDPRO_NUMPED");
                    String cod_art = rs.getString("PEDPRO_ART");
                    int cant = rs.getInt("PEDPRO_CANT_UOM");
                    String sCant = Integer.toString(cant); //convierto int a string
                    String dun14 = rs.getString("DUN14");
                    int cap_dun = rs.getInt("CAP_DUN");
                    String sCap_dun = Integer.toString(cap_dun);
                    String cod_ean = rs.getString("COD_EAN");
                    String desc_lote = rs.getString("DESCRIP");
                    String desc_art = rs.getString("DESCRIPCION");
                    jText_pedproProv.setText(rs.getString("PEDPRO_PROV") + " " + rs.getString("PEDPRO_RAZON_SOCIAL"));
                    //rellenaTextArea(2, cod_art, sCant, dun14, sCap_dun, cod_ean);
                    //relleno la jtable
                    model.addRow(new Object[]{cod_art,sCant,dun14, sCap_dun, cod_ean, desc_lote, desc_art, false}); 
               
                                   
                }
               
                //zpl();
            }
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
    }
    
    private void rellenar_albaran_normal(){
        
        jComboBox_AlbNormal.removeAllItems();
        
        try {

         
             String query = "SELECT TOP 100 DEL.SOHNUM_0 AS PEDIDO_DENOX, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPCORD_0, DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0, \n"
                    + "ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0, VAL2.PREUSR_0 AS PREPARADOR \n"
                    + ", CASE (SUBSTRING(DEL.SDHNUM_0, 1,3)) WHEN 'ALD' THEN 'Z' WHEN 'ALB' THEN 'A' ELSE 'D' END AS ORDEN \n" 
                    + "fROM LIVE.SDELIVERY DEL\n"
                    + "INNER JOIN LIVE.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN LIVE.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN LIVE.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN LIVE.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN LIVE.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
                    + "WHERE DEL.STOFCY_0 = '" + buttonGroup_alb.getSelection().getActionCommand() + "' AND DEL.SDHNUM_0 NOT LIKE 'ALD" + "%'\n"
                    + "ORDER BY ORDEN, DEL.SDHNUM_0 DESC";
            

            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            
            jComboBox_AlbNormal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ALB FAM-TRI" }));
                      
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_AlbNormal.addItem(rs.getString("NUMALB") + " " + rs.getString("CTE"));
                                 
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
    }
    
    private void rellenar_albaran_sscc(){
        
        jComboBox_AlbSSCC.removeAllItems();
        
        try {

         
             String query = "SELECT TOP 100 DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPCORD_0, DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0, \n"
                    + "ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0, VAL2.PREUSR_0 AS PREPARADOR \n"
                    + ", CASE (SUBSTRING(DEL.SDHNUM_0, 1,3)) WHEN 'ALD' THEN 'Z' WHEN 'ALB' THEN 'A' ELSE 'D' END AS ORDEN \n" 
                    + "fROM LIVE.SDELIVERY DEL\n"
                    + "INNER JOIN LIVE.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN LIVE.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN LIVE.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN LIVE.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN LIVE.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
                    + "INNER JOIN LIVE.SPACK PAC ON PAC.VCRNUM_0 = DEL.SDHNUM_0\n" 
                    + "WHERE DEL.STOFCY_0 = '" + buttonGroup_SSCC.getSelection().getActionCommand() + "' AND DEL.SDHNUM_0 NOT LIKE 'ALD" + "%'\n"
                    +" GROUP BY DEL.SOHNUM_0, DEL.BPCORD_0, DEL.SDHNUM_0, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPAADD_0\n" +
"					  , BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0\n" +
"					  , ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0, VAL2.PREUSR_0 \n"
                    + "ORDER BY ORDEN, DEL.SDHNUM_0 DESC";
            

            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            
            jComboBox_AlbSSCC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ALB FAM-TRI" }));
                      
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_AlbSSCC.addItem(rs.getString("NUMALB") + " " + rs.getString("CTE"));
                                 
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
    }
    private void consultar_albaran_normal() {
        //LLenamos el ComboBox de albaranes
        
        //System.out.println ("CONSULTAR ALBARAN NOORMAL" +buttonGroup_alb.getSelection().getActionCommand() );
        jTextArea4.setText(null);
        try {

           
             String query = "SELECT DEL.SOHNUM_0 AS PEDIDO_DENOX, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0 AS TRANSPORTISTA, DEL.BPCORD_0,\n"
                    + " DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0 as DESTINO, ORD.CUSORDREF_0 AS NUMPED, DEL.YPACK_0 AS BULTOS\n"
                    //+ "ADR.BPAADDLIG_0 AS DIR1, ADR.BPAADDLIG_1 AS DIR2, ADR.POSCOD_0 AS CP, ADR.CTY_0 AS CITY, ADR.SAT_0 AS PROVINCIA, ADR.CRYNAM_0 AS PAIS\n"
                    + ", DEL.BPDADDLIG_0 AS DIR1, DEL.BPDADDLIG_1 AS DIR2, DEL.BPDPOSCOD_0 AS CP, DEL.BPDCTY_0 AS CITY, DEL.BPDSAT_0 AS PROVINCIA, DEL.BPDCRYNAM_0 AS PAIS\n"
                    + ", VAL2.PREUSR_0 AS PREPARADOR\n"
                    + " ,CASE (SUBSTRING(DEL.SDHNUM_0, 1,3)) WHEN 'ALD' THEN 'Z' WHEN 'ALB' THEN 'A' ELSE 'D' END AS ORDEN \n"
                    + "fROM LIVE.SDELIVERY DEL\n"
                    + "INNER JOIN LIVE.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN LIVE.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN LIVE.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN LIVE.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN LIVE.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
                    + "WHERE DEL.SDHNUM_0='" + jTextField_AlbNumAlb.getText() + "'" + "\n"
                    + "AND DEL.STOFCY_0 = '" + buttonGroup_alb.getSelection().getActionCommand() + "'"
                    + "ORDER BY ORDEN, DEL.SDHNUM_0 DESC";
            //String query = "select SOHNUM_0 as NUMALB, BPCORD_0 AS CTE FROM FAMESAOF.SDELIVERY ORDER BY SOHNUM_0 DESC";
                                     
             rs = stmt.executeQuery(query);//rs contendrá todos los registros
             
            //zpl();
            rs.next();

            //while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
            //jComboBox_AlbNormal.addItem(rs.getString("NUMALB") + " " + rs.getString("CTE"));
            if (rs.getRow() != 0) {
                System.out.println("entrando..." + "registro: "+rs.getRow() + " " );
                jTextField_AlbCte.setText(rs.getString("CTE"));
                jTextField_AlbNomCte.setText(rs.getString("DESTINO"));
                jTextField_AlbNumAlb.setText(rs.getString("NUMALB"));
                jTextField_AlbPedidoCte.setText(rs.getString("NUMPED"));
                jTextField_AlbTrans.setText(rs.getString("TRANSPORTISTA"));
                jTextField_AlbDirEnv.setText(rs.getString("DIR1"));
                jTextField_AlbBultos.setText(rs.getString("BULTOS"));
                jTextField_AlbDir2.setText(rs.getString("DIR2"));
                jTextField_AlbDirPostal.setText(rs.getString("CP") + " " + rs.getString("CITY") + " " + rs.getString("PROVINCIA"));
                jTextField_AlbPrep.setText(rs.getString("PREPARADOR"));
                zpl(4,Integer.parseInt(jTextField_AlbBultos.getText()),0,"");
                System.out.println("salgo...");
            }
            else{
                
                JOptionPane.showMessageDialog(this, "no existe registro para ese albarán");
            }

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
            //zpl();
            //}
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
    }
    private void jButton_consultar_registroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_consultar_registroActionPerformed
        // dada una referencia y cliente pinto los datos en los campos correspondientes al hacer click en el botón "consultar". Posiblemente elmine este evento
        try {
            
            // JOptionPane.showMessageDialog(this, System.getProperty("user.dir"));                     
            //busco el idioma seleccionado parar añadir la condición a la query
            String idioma;
            if (jRadioButton1.isSelected()){
                idioma = jRadioButton1.getName();
                     
            }
            else if (jRadioButton2.isSelected()){
                idioma = jRadioButton2.getName();
            }
            else
                idioma = jRadioButton3.getName();
                    
            
            String query = "select REFERENCIA, CLIENTE, NOM_CTE, DESCRIP, EAN, TEXTO, CAPACIDAD_DUN\n"
                    + "FROM V_ARTI_DUN14_TRAD_NEW\n"
                    + "WHERE REFERENCIA = '" + jText_referencia.getText() + "'\n"
                    + "AND CLIENTE = '" + jText_ref_cliente.getText() + "'\n"
                    + "AND LENGUAJE = '" + idioma + "'";
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            
            while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en los campos
                jText_ref_desc_art.setText(rs.getString("TEXTO"));
                //jText_ref_codcte.setText(rs.getString("CLIENTE"));
                jText_ref_capacidad.setText(rs.getString("CAPACIDAD_DUN"));
                jText_ref_desc_cte.setText(rs.getString("NOM_CTE"));
                jText_ref_ean.setText(rs.getString("EAN"));
                zpl(2,Integer.parseInt(jFormattedTextField_cantidad.getText()),0,""); //le paso la cantidad de etiquetas a imprimir
                System.out.println(rs.getString("DESCRIP") + " " + rs.getString("CLIENTE") + " " + rs.getString("NOM_CTE") + " " + rs.getString("EAN") + " " + rs.getString("CAPACIDAD_DUN"));

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
                //zpl();
            }
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
    }//GEN-LAST:event_jButton_consultar_registroActionPerformed
    private void consultar_of() {
        //LLenamos el ComboBox de albaranes
        System.out.println ("consultar OF");
        jTextArea6.setText(null);
        try {

          
             String query = "select top 1 CAB.MFGNUM_0 AS NUM_OF, CAST(CAB.EXTQTY_0 AS INTEGER) AS CANT_PREVISTA, MGI.ITMREF_0 AS ARTI, ITM.ITMDES1_0 AS DESCRIP\n"
                            +", CAST(CAB.EXTQTY_0 /  CASE (ITM.PCUSTUCOE_0) WHEN 0 THEN 1"
                            +"                                    ELSE ISNULL(ITM.PCUSTUCOE_0,1) END AS INTEGER) AS CANT_CAJA\n" 
                            +", (CAST(CAB.EXTQTY_0 / CASE (ITF.PCKCAP_0) WHEN 0 THEN 1\n"
                            +"                                    ELSE ISNULL(ITF.PCKCAP_0,1) END AS INTEGER) )\n"
		            +"					+ CASE(MGI.UOMEXTQTY_0 % CASE (ITF.PCKCAP_0) WHEN 0 THEN 1 ELSE ITF.PCKCAP_0 END) WHEN 0 THEN 0 ELSE 1 END\n"
			    +"                                      AS CANT_PALLET\n"
                            +", ITM.EANCOD_0 AS EANCOD, MGI.LOT_0 AS LOTE\n"
                            +", CASE(ITS.ITMTYP_0) WHEN 1 THEN 'NORMAL' WHEN 3 THEN 'KIT' ELSE 'ESTRUCTURA' END AS KIT\n"
                            +", CAST(ITF.PCKCAP_0 AS INTEGER) as CAPACIDAD_PALLET\n"
                            +", CAST(MGI.UOMEXTQTY_0 % CASE (ITF.PCKCAP_0) WHEN 0 THEN 1 ELSE ITF.PCKCAP_0 END AS INTEGER) AS PICO_PALLET\n"
                            +", CAST(ITM.PCUSTUCOE_0 AS INTEGER) AS CAPACIDAD_CAJA\n"
                            +", CAST(CAB.EXTQTY_0 % CASE (ITM.PCUSTUCOE_0) WHEN 0 THEN 1 ELSE ITM.PCUSTUCOE_0 END AS INTEGER) AS PICO_CAJA\n"
                            +", BOM.ITMREF_0 AS PADRE\n"
                            +"FROM LIVE.MFGHEAD CAB\n"
                            +"INNER JOIN LIVE.MFGITM MGI ON MGI.MFGNUM_0 = CAB.MFGNUM_0\n"
                            +"INNER JOIN LIVE.ITMMASTER ITM ON MGI.ITMREF_0 = ITM.ITMREF_0\n"
                            +"INNER JOIN LIVE.ITMSALES ITS ON ITS.ITMREF_0 = ITM.ITMREF_0\n"
                            +"INNER JOIN LIVE.ITMFACILIT ITF ON ITF.ITMREF_0 = ITM.ITMREF_0\n"
                            +"LEFT JOIN LIVE.BOMD BOM ON BOM .CPNITMREF_0 = ITM.ITMREF_0\n"
                            +"WHERE CAB.MFGNUM_0='" + jTextField_OF.getText() + "'";
                 
                                                
             rs = stmt.executeQuery(query);//rs contendrá todos los registros
                      
            rs.next();

            //while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
            //jComboBox_AlbNormal.addItem(rs.getString("NUMALB") + " " + rs.getString("CTE"));
            if (rs.getRow() != 0) {
                System.out.println(" entrando..." + "registro: "+rs.getRow() + " " );
                jTextField_OFarti.setText(rs.getString("ARTI"));
                jTextField_OFdescrip.setText(rs.getString("DESCRIP"));
                jTextField_OfEan.setText(rs.getString("EANCOD"));
                jFormattedTextField_CantOF.setText(rs.getString("CANT_PREVISTA"));
                jFormattedTextField_CantCaja.setText(rs.getString("CANT_CAJA"));
                jFormattedTextField_CantPallet.setText(rs.getString("CANT_PALLET"));
                jFormattedTextField_CapPallet.setText(rs.getString("CAPACIDAD_PALLET"));
                jFormattedTextField_Pico.setText(rs.getString("PICO_PALLET"));
                jFormattedTextField_CapCaja.setText(rs.getString("CAPACIDAD_CAJA"));
                jFormattedTextField_PicoCaja.setText(rs.getString("PICO_CAJA"));
                jTextField_OfLote.setText(rs.getString("LOTE"));
                jTextField_OfKit.setText(rs.getString("KIT"));
                jTextField_PadreArti.setText(rs.getString("PADRE"));
                
                System.out.println("antes de buscar padre");
                //JOptionPane.showMessageDialog(this, jTextField_PadreArti.getText());
                if (jTextField_PadreArti.getText().isEmpty()){
                    System.out.println("huerfano");
                    jComboBox_Padres.removeAllItems();
                    jTextField_PadreEan.setText("");
                    jTextField_PadreDesc.setText("");
                    jFormattedTextField_CantPadreCaja.setText("");
                    jFormattedTextField_CantPadrePallet.setText("");
                    jPanel20.setEnabled(false);
                    jPanel20.setVisible(false);
                    jRadioButton_Padre.setEnabled(false);
                    jRadioButton_OF.setSelected(true);
                    
                } else {
                    //jComboBox_Padres.removeAllItems();
                    
                    System.out.println("tiene padre");
                    rellenar_combo_padre();
                    consultar_padre(0);
                    
                 }
                //zpl(4,Integer.parseInt(jTextField_AlbBultos.getText()),0); AQUÍ LA LLAMADA A GENERAR EL CÓDIGO DE LA ETIQUETA
                System.out.println("salgo...");
            }
            else{
                
                JOptionPane.showMessageDialog(this, "no existe registro para esa OF");
            }

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
            //zpl();
            //}
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
    }
    
    
    private void consultar_padre(int pTipo) {
        //Relleno las cantidades de etiquetas del padre
        System.out.println ("consultar Padre");
        //jTextArea6.setText(null);
                
        try {

          
             String query = "SELECT ITM.ITMREF_0 AS ARTI_PADRE, ITM.EANCOD_0 AS EAN_PADRE, ITM.ITMDES1_0 AS DESC_PADRE"
                            + ",  CAST(" + jFormattedTextField_CantOF.getText() + " / CASE ITM.PCUSTUCOE_0 WHEN 0 THEN 1\n" 
                            +"  ELSE   ITM.PCUSTUCOE_0 END AS INTEGER) AS CANT_PADRE_CAJA \n" 
                            + ",  CAST(" + jFormattedTextField_CantOF.getText() + " / CASE ITF.PCKCAP_0 WHEN 0 THEN 1\n" 
                            +"  ELSE   ITF.PCKCAP_0 END AS INTEGER) AS CANT_PADRE_PALLET\n" 
                            +"FROM LIVE.ITMMASTER ITM\n" 
                            +"INNER JOIN LIVE.ITMFACILIT ITF ON ITF.ITMREF_0 = ITM.ITMREF_0\n" 
                            +"WHERE ITM.ITMREF_0 = '" + jTextField_PadreArti.getText() + "'";
                 
                                                
            
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
                      
            rs.next();

            //while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
            
            if (rs.getRow() != 0) {
                System.out.println(" Entrando..." + "registro: "+rs.getRow() + " " );
                jTextField_PadreArti.setText(rs.getString("ARTI_PADRE"));
                jTextField_PadreDesc.setText(rs.getString("DESC_PADRE"));
                jTextField_PadreEan.setText(rs.getString("EAN_PADRE"));
                //jFormattedTextField_CantPadre.setText(rs.getString("CANT_PREVISTA"));
                jFormattedTextField_CantPadreCaja.setText(rs.getString("CANT_PADRE_CAJA"));
                jFormattedTextField_CantPadrePallet.setText(rs.getString("CANT_PADRE_PALLET"));
                      
                
                //zpl(4,Integer.parseInt(jTextField_AlbBultos.getText()),0); AQUÍ LA LLAMADA A GENERAR EL CÓDIGO DE LA ETIQUETA
                
                System.out.println("muestro panel");
                jPanel20.setEnabled(true);
                jPanel20.setVisible(true);
                jRadioButton_Padre.setEnabled(true);
                if (pTipo == 1){
                    jFormattedTextField_CapPallet.setText(rs.getString("CAPACIDAD_PALLET"));
                    jFormattedTextField_Pico.setText(rs.getString("PICO_PALLET"));
                    jFormattedTextField_CapCaja.setText(rs.getString("CAPACIDAD_CAJA"));
                    jFormattedTextField_PicoCaja.setText(rs.getString("PICO_CAJA"));
                } 
                
            }
            else{
                
                //JOptionPane.showMessageDialog(this, "no tiene Padre");
                //jComboBox_Padres.removeAllItems();
                System.out.println("no tiene padre, oculto panel");
                jPanel20.setEnabled(false);
                jPanel20.setVisible(false);
                jRadioButton_Padre.setEnabled(false);
                jRadioButton_OF.setSelected(true);
                
            }

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
            //zpl();
            //}
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
    }
   
   private void rellenar_combo_padre(){//Relleno el combobox de padres con el artículo de la OF
       
       //System.out.println("borro lista padres");
       System.out.println("nº padres antes de borrado: " + jComboBox_Padres.getComponentCount());
       jComboBox_Padres.removeAllItems();       
       jComboBox_Padres.addItem("Padres");
       System.out.println("nº padres después de borrado: " + jComboBox_Padres.getComponentCount());
    
             
             
       try {

             String query = "SELECT DISTINCT ITMREF_0 AS PADRE\n" 
                     +       "FROM LIVE.BOMD\n" 
                     +       "WHERE CPNITMREF_0 ='" + jTextField_OFarti.getText() + "'";
             
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();

            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_Padres.addItem(rs.getString("PADRE"));
                 jTextField_PadreArti.setText(rs.getString("PADRE"));
                 
                 //jComboBox_Padres.setSelectedIndex(-1);
                             
            }
        //jTextField_PadreArti.setText(jComboBox_Padres.getItemAt(0).toString());

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
       
   } 
   
     private void consultar_EtqSimple_ref() {
        //ejecución de la consulta de referencia, pinto los datos en los campos correspondientes, añadido para alternativa a botón de consulta y meterlo en 
        //el evento de cambio de estado del combobox.    
         jTextArea8.setText(null);
         try {
                            
             String query = "SELECT ITM.ITMREF_0 AS REF, ITM.ZITMREFSC_0 AS REFSC, ITM.EANCOD_0 AS EAN, ATX.TEXTE_0 AS DESCRIP FROM \n" 
                            + "LIVE.ITMMASTER ITM \n" 
                            + "INNER JOIN LIVE.ATEXTRA ATX ON ATX.IDENT1_0 = ITM.ITMREF_0 \n" 
                            + "AND ITM.ITMREF_0 ='" + jText_EtqSimple_ref.getText() + "'\n"
                            + "WHERE ATX.CODFIC_0 = 'ITMMASTER' \n"  
                            + "AND ATX.ZONE_0 = 'DES1AXX' \n"
                            + "AND ATX.LANGUE_0 = UPPER('" + jTextField_EtqSimple_lan.getText() + "')";
            
             rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();
            rs.next(); //muevo el cursor al primer registro y muestro los datos en los campos
            
            if (rs.getRow()!= 0){//comprueba que haya registro en rs para evitar el error de que coja el campo descriptivo en posición 1
            jText_EtqSimple_descrip.setText(rs.getString("DESCRIP"));
            jText_EtqSimple_Ean.setText(rs.getString("EAN"));
            //zpl(2,Integer.parseInt(jFormattedTextField_cantidad.getText()),0,""); //le paso la cantidad de etiquetas a imprimir
            
            }
            else
                JOptionPane.showMessageDialog(this, "no existe registro para referencia: [" + jText_EtqSimple_descrip.getText() + "] e idioma: [" + jTextField_EtqSimple_lan.getText() + ']');

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
    }
     
      private void rellenar_ElCorte(){
        //jCombo_ElCorte.removeAllItems();       
                 
             
       try {

           String query = "select TOP 100 EDI.YCENTRO_0 AS SUCURSAL, EDI.YSECCION_0 AS DEPART, PED.CUSORDREF_0 AS PDO_CTE, CAB.SDHNUM_0 AS ALB, DET.ITMREF_0 AS ARTI"
                            + ", DET.ITMDES_0 AS DESCRIP_ARTI\n" +
                            ", COL.DESCOD_0 AS COLOR, ACT.ITMREFBPC_0 AS ARTI_CTE, ACT.ITMREFBPC_0\n" + //ACT.ZITMREFBPC_0\n" + elimino este campo por no existir en v12
                            "from LIVE.SDELIVERY CAB\n" +
                            "INNER JOIN LIVE.SDELIVERYD DET ON DET.SDHNUM_0 = CAB.SDHNUM_0\n" +
                            "INNER JOIN LIVE.YSORDEREDI EDI ON EDI.SOHNUM_0 = DET.SOHNUM_0\n" +
                            "INNER JOIN LIVE.SORDER PED ON PED.SOHNUM_0 = DET.SOHNUM_0\n" +
                            "LEFT JOIN LIVE.ITMBPC ACT ON ACT.ITMREF_0 = DET.ITMREF_0 AND ACT.BPCNUM_0 = CAB.BPCORD_0\n" +
                            "LEFT OUTER JOIN LIVE.TABCTL COL ON COL.ALPCOD_0 = RIGHT(DET.ITMREF_0,3)\n" +
                            //"WHERE CAB.BPCORD_0 = '" + jTextField_ElCorte_Cte.getText()+ "'\n" +
                            "WHERE CAB.BPCGRU_0 = '" + jTextField_ElCorte_Cte.getText() + "'\n" + //añado el grupo en lugar del cliente porque se expide a varios El Corte...
                            //"AND COL.TCT_0 = 'COL'\n" +
                            "ORDER BY CAB.SDHNUM_0 DESC";
                                   
           
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();

            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jCombo_ElCorte.addItem(rs.getString("ALB")+ " " + rs.getString("ARTI"));
                
            }
        
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }

      }
     
     
        private void consultar_ElCorte(){//Relleno el combobox de albaranes para el corte inglés
       
        jTextArea9.setText(null); //vacío el TextArea para que no se apilen las etiquetas
        System.out.println("Consultando ElCorte - " + jText_ElCorte_Alb.getText());
       
                 
             
       try {

           String query = "select EDI.YCENTRO_0 AS SUCURSAL, EDI.YSECCION_0 AS DEPART, PED.CUSORDREF_0 AS PDO_CTE"
                            + ", CONCAT (SUBSTRING(CAB.SDHNUM_0,5,2), SUBSTRING(CAB.SDHNUM_0, CHARINDEX('-', CAB.SDHNUM_0, 8) +2, LEN(CAB.SDHNUM_0)))  AS ALB, DET.ITMREF_0 AS ARTI"
                            + ", DET.ITMDES_0 AS DESCRIP_ARTI\n" +
                            ", COL.DESCOD_0 AS COLOR, SUBSTRING(ACT.ITMREFBPC_0, CHARINDEX('-', ACT.ITMREFBPC_0) +1, LEN(ACT.ITMREFBPC_0)) AS ARTI_CTE\n" + //, ACT.ZITMREFBPC_0\n" +
                            ", CAB.SDHNUM_0 AS NUMALB\n" +
                            "from LIVE.SDELIVERY CAB\n" +
                            "INNER JOIN LIVE.SDELIVERYD DET ON DET.SDHNUM_0 = CAB.SDHNUM_0\n" +
                            "INNER JOIN LIVE.YSORDEREDI EDI ON EDI.SOHNUM_0 = DET.SOHNUM_0\n" +
                            "INNER JOIN LIVE.SORDER PED ON PED.SOHNUM_0 = DET.SOHNUM_0\n" +
                            "LEFT JOIN LIVE.ITMBPC ACT ON ACT.ITMREF_0 = DET.ITMREF_0 AND ACT.BPCNUM_0 = CAB.BPCORD_0\n" +
                            "LEFT OUTER JOIN LIVE.TABCTL COL ON COL.ALPCOD_0 = RIGHT(DET.ITMREF_0,3)\n" +
                            "WHERE CAB.SDHNUM_0 = '" + jText_ElCorte_Alb.getText() + "'\n" + 
                            "AND DET.ITMREF_0 ='"+ jTextField_ElCorte_Art.getText() + "'";
                            //"AND COL.TCT_0 = 'COL'";
             
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            
            //zpl();
            
            rs.next(); //Muevo el cursor al primer registro
            if (rs.getRow()!= 0){//comprueba que haya registro en rs para evitar el error de que coja el campo descriptivo en posición 1
                 jText_ElCorte_Descrip.setText(rs.getString("DESCRIP_ARTI"));
                 jText_ElCorte_RefCte.setText(rs.getString("ARTI_CTE"));
                 jTextField_ElCorte_Color.setText(rs.getString("COLOR"));
                 jTextField_ElCorte_Sucursal.setText(rs.getString("SUCURSAL"));
                 jTextField_ElCorte_Depart.setText(rs.getString("DEPART"));
                 jTextField_ElCorte_Ped.setText(rs.getString("PDO_CTE"));
                 jTextField_ElCorte_Alb2.setText((rs.getString("ALB")));
                 jText_ElCorte_Alb.setText((rs.getString("NUMALB")));
            }
            else
                JOptionPane.showMessageDialog(this, "registro no encontrado");
            
//           
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
       
   } 
        public void rellenar_StockLotes(String pPlanta){
           try {
            jComboBox_Stock.removeAllItems();
            System.out.println("elementos del combo eliminados");
            String query ="select ITMREF_0 AS ARTI, LOT_0 AS LOTE, MIN(SLO_0) AS MINLOT, MAX(SLO_0) AS MAXLOT FROM LIVE.STOCK\n"
                    + "WHERE STOFCY_0 = '" + pPlanta + "'\n"
                    + "GROUP BY ITMREF_0, LOT_0 ORDER BY ITMREF_0";
            
            
           
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();

            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_Stock.addItem(rs.getString("ARTI")+ " " + rs.getString("LOTE"));
                
            }
        
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }   
        }
        
        public void consultar_StockLotes(){
        String vPlanta;
        //busco las ubicaciones entre el mínimo y el máximo 
        System.out.println("consultar Stocks Artículos");
        if (jRadioButton_PlantaF.isSelected())
              vPlanta = "FAM";
        else
            vPlanta ="TRI";
        
        try {

            String query ="select ITMREF_0, LOT_0, MIN(SLO_0) AS MINLOT, MAX(SLO_0) AS MAXLOT FROM LIVE.STOCK\n"
                    + "WHERE ITMREF_0 = '" + jTextStock_arti.getText() + "' AND LOT_0 = '" + jTextStock_lote.getText() + "'\n"
                    + " and STOFCY_0 = '" + vPlanta + "'\n"
                    + "GROUP BY ITMREF_0, LOT_0";
             
           
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
          
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                jText_StockDesde.setText(rs.getString("MINLOT"));
                jText_StockHasta.setText(rs.getString("MAXLOT"));
                
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        } 
        
    }
        public void check_radiobutton_stocks(){
          //AL CAMBIAR DE RADIOBUTTON RELLENO EL COMBO CORRESPONDIENTE A LA PLANTA SELECCIONADA
        jComboBox_Ubi.removeAllItems();
         if (jRadioButton_PlantaF.isSelected()){
            System.out.println("stock famesa...");
            rellenar_StockLotes("FAM");
        }else if(jRadioButton_PlantaT.isSelected()){
            System.out.println("stocks trilla");
            rellenar_StockLotes("TRI");
        }
    }
        
        
         private void consultar_alb_dascher(int pModo) {
        
        try {
          String query; 
          //pModo == 1 aplico filtro por campo en el formulario, pModo<>1 saco todos los albaranes de dascher

            if (pModo == 1) { 
            query = "SELECT TOP 100  SDHNUM_0 AS NUMALB, '430841156' as COD_REMITE, SDHNUM_0, ORD.CUSORDREF_0 AS PD_CTE\n" +
                ", SUBSTRING(DEL.BPDNAM_0,1,32) AS NOM_CONSIG,  SUBSTRING(CONCAT(DEL.BPDADDLIG_0, DEL.BPDADDLIG_1, DEL.BPDADDLIG_2),1,32) AS DIR_CONSIG\n" +
                ", SUBSTRING(CONCAT(DEL.BPDADDLIG_0, DEL.BPDADDLIG_1, DEL.BPDADDLIG_2),33,62) AS DIR_2\n" +    
                ", DEL.BPDPOSCOD_0 AS CP_CONSIG, DEL.BPDCTY_0 AS POB_CONSIG, DEL.BPDCRY_0 AS PAIS_CONSIG\n" +
                ", CONCAT(REPLICATE('0', 3 - LEN(PACNBR_0)), PACNBR_0) AS BULTOS\n" +
                ", CONCAT(REPLICATE('0', 5 - LEN(CAST(GROWEI_0 AS INTEGER))), CAST(GROWEI_0 AS INTEGER)) AS PESO\n" +
                ", CONCAT(REPLICATE('0', 5 - LEN(CAST(VOL_0 AS DECIMAL(10,2)))), CAST(VOL_0 AS DECIMAL(10,3))) AS VOLUMEN\n" +
                ", CASE  DEL.YGPORTES_0 WHEN  1 THEN 'P'\n" +
                "				   WHEN  2 THEN 'D'\n" +
                "				   ELSE 'X' END AS PORTES\n" +
                ", ADR.WEB_0 AS MAIL_CONSIG, ADR.TEL_0 AS TFNO_CONSIG, ZLOG.FECHA, DEL.DLVDAT_0 AS FEC_ENTREGA, DEL.YPACK_0 AS BULTOS2\n" +
                "fROM LIVE.SDELIVERY DEL\n" +
                "INNER JOIN LIVE.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n" + 
                "INNER JOIN LIVE.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND ADR.BPAADD_0 = DEL.BPAADD_0\n" +
                "LEFT JOIN LIVE.ZSDELIVERY_PRINT_LOG ZLOG ON ZLOG.CODIGO = DEL.SDHNUM_0 AND  TIPO = 'DASCHER'\n" +   
                "WHERE DEL.BPTNUM_0 IN ('025','20','036') AND DEL.SALFCY_0 = 'FAM'\n" +
                "ORDER BY SUBSTRING(SDHNUM_0,1,3), SDHNUM_0 DESC";
            }
            else{
            query = "SELECT TOP 100  SDHNUM_0 AS NUMALB, '430841156' as COD_REMITE, SDHNUM_0, ORD.CUSORDREF_0 AS PD_CTE\n" +
                ", SUBSTRING(DEL.BPDNAM_0,1,32) AS NOM_CONSIG,  SUBSTRING(CONCAT(DEL.BPDADDLIG_0, DEL.BPDADDLIG_1, DEL.BPDADDLIG_2),1,32) AS DIR_CONSIG\n" +
                ", SUBSTRING(CONCAT(DEL.BPDADDLIG_0, DEL.BPDADDLIG_1, DEL.BPDADDLIG_2),33,62) AS DIR_2\n" +    
                ", DEL.BPDPOSCOD_0 AS CP_CONSIG, DEL.BPDCTY_0 AS POB_CONSIG, DEL.BPDCRY_0 AS PAIS_CONSIG\n" +
                ", CONCAT(REPLICATE('0', 3 - LEN(PACNBR_0)), PACNBR_0) AS BULTOS\n" +
                ", CONCAT(REPLICATE('0', 5 - LEN(CAST(GROWEI_0 AS INTEGER))), CAST(GROWEI_0 AS INTEGER)) AS PESO\n" +
                ", CONCAT(REPLICATE('0', 5 - LEN(CAST(VOL_0 AS DECIMAL(10,2)))), CAST(VOL_0 AS DECIMAL(10,3))) AS VOLUMEN\n" +
                ", CASE  DEL.YGPORTES_0 WHEN  1 THEN 'P'\n" +
                "				   WHEN  2 THEN 'D'\n" +
                "				   ELSE 'X' END AS PORTES\n" +
                ", ADR.WEB_0 AS MAIL_CONSIG, ADR.TEL_0 AS TFNO_CONSIG, ZLOG.FECHA, DEL.DLVDAT_0 AS FEC_ENTREGA, DEL.YPACK_0 AS BULTOS2\n" +
                "fROM LIVE.SDELIVERY DEL\n" +
                "INNER JOIN LIVE.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n" +   
                "INNER JOIN LIVE.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND ADR.BPAADD_0 = DEL.BPAADD_0\n" +
                "LEFT JOIN LIVE.ZSDELIVERY_PRINT_LOG ZLOG ON ZLOG.CODIGO = DEL.SDHNUM_0 AND  TIPO = 'DASCHER'\n" +   
                "WHERE DEL.BPTNUM_0 IN ('025','20', '036') AND DEL.SALFCY_0 = 'FAM'\n" +
                "AND SDHNUM_0 LIKE '" + jText_alb_dascher.getText() + "'\n" +
                "ORDER BY SDHNUM_0 DESC";
                 }
            
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            
                        
            inicio_tabla_alb_dascher();//Método de inicio de jtable
                   
                               
            while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en el textArea
                if (rs.getRow() != 0) {//comprueba que haya registro en rs para evitar el error de que coja el campo descriptivo en posición 1

                    String cod_remite = rs.getString("COD_REMITE");
                    String num_alb = rs.getString("NUMALB");
                    String nom_consig = rs.getString("NOM_CONSIG");
                    //int cant = rs.getInt("PEDPRO_CANT_UOM");
                    String dir_consig = rs.getString("DIR_CONSIG");
                    //String sCant = Integer.toString(cant); //convierto int a string
                    String cp_consig = rs.getString("CP_CONSIG");
                    //int cap_dun = rs.getInt("CAP_DUN");
                    //String sCap_dun = Integer.toString(cap_dun);
                    String pob_consig = rs.getString("POB_CONSIG");
                    String pais_consig = rs.getString("PAIS_CONSIG");
                    String sBultos = rs.getString("BULTOS");
                    //int bultos = rs.getInt("BULTOS");
                    //String sBultos = Integer.toString(bultos); //convierto int a string
                    //int peso = rs.getInt("PESO");
                    //String sPeso = Integer.toString(peso);
                    String sPeso = rs.getString("PESO");
                    String portes  = rs.getString("PORTES");
                    String mail_consig = rs.getString("MAIL_CONSIG");
                    String tfno_consig = rs.getString("TFNO_CONSIG");
                    Date fecha_log = rs.getDate("FECHA");
                    Date fecha_entrega = rs.getDate("FEC_ENTREGA");
                    String dir2 = rs.getString("DIR_2");
                    String sVolumen = rs.getString("VOLUMEN");
                    String pdCte = rs.getString("PD_CTE");
                    String sBultos2 = rs.getString("BULTOS2");
                    
                    //jText_pedproProv.setText(rs.getString("PEDPRO_PROV") + " " + rs.getString("PEDPRO_RAZON_SOCIAL"));
                    //rellenaTextArea(2, cod_art, sCant, dun14, sCap_dun, cod_ean);
                    //relleno la jtable
                    //model.addRow(new Object[]{cod_art,sCant,dun14, sCap_dun, cod_ean, desc_lote, desc_art, false}); 
                    //System.out.println("Relleno combo Alb dascher");
                    //jCombo_alb_dasch.addItem(rs.getString("NUMALB")+ " " + rs.getString("NOM_CONSIG"));
//                    System.out.println("Relleno tabla dascher");
                    model.addRow(new Object[]
                    {cod_remite, num_alb, nom_consig,dir_consig, cp_consig, pob_consig, pais_consig, sBultos, sPeso, portes, mail_consig, tfno_consig ,false, fecha_log, fecha_entrega,"","", dir2, "", sVolumen, pdCte, sBultos2}); 
               
                                   
                }
               
                //zpl();
            }
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
    }
         
    private void consultar_albaran_sscc() {
        //LLenamos el ComboBox de albaranes
        
        System.out.println ("CONSULTAR ALBARAN SSCC" +buttonGroup_SSCC.getSelection().getActionCommand() );
        jTextArea12.setText(null);
        try {

           
             String query = "SELECT DEL.SOHNUM_0 AS PEDIDO_DENOX, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0 AS TRANSPORTISTA, DEL.BPCORD_0,\n"
                    + " DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0 as DESTINO, ORD.CUSORDREF_0 AS NUMPED, DEL.YPACK_0 AS BULTOS, DEL.PACNBR_0 AS PALLETS\n"
                    //+ "ADR.BPAADDLIG_0 AS DIR1, ADR.BPAADDLIG_1 AS DIR2, ADR.POSCOD_0 AS CP, ADR.CTY_0 AS CITY, ADR.SAT_0 AS PROVINCIA, ADR.CRYNAM_0 AS PAIS\n"
                    + ", DEL.BPDADDLIG_0 AS DIR1, DEL.BPDADDLIG_1 AS DIR2, DEL.BPDPOSCOD_0 AS CP, DEL.BPDCTY_0 AS CITY, DEL.BPDSAT_0 AS PROVINCIA, DEL.BPDCRYNAM_0 AS PAIS\n"
                    + ", VAL2.PREUSR_0 AS PREPARADOR\n"
                    + " ,CASE (SUBSTRING(DEL.SDHNUM_0, 1,3)) WHEN 'ALD' THEN 'Z' WHEN 'ALB' THEN 'A' ELSE 'D' END AS ORDEN \n"
                    + "fROM LIVE.SDELIVERY DEL\n"
                    + "INNER JOIN LIVE.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN LIVE.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN LIVE.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN LIVE.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN LIVE.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
                    + "INNER JOIN LIVE.SPACK PAC ON PAC.VCRNUM_0 = DEL.SDHNUM_0\n" 
                    + "WHERE DEL.SDHNUM_0='" + jTextField_AlbNumAlb1.getText() + "'" + "\n"
                    + "AND DEL.STOFCY_0 = '" + buttonGroup_SSCC.getSelection().getActionCommand() + "'"
                    + "ORDER BY ORDEN, DEL.SDHNUM_0 DESC";
             
             //System.out.println("Ejecuto procedimiento LIVE.ZLANZA_ETQSCC_AUTO");
             //String procedure = "EXECUTE  [LIVE].[ZLANZA_ETQSCC_AUTO] '" +  jTextField_AlbNumAlb1.getText() + ", 'ZZMARIO'";
                                                 
             rs = stmt.executeQuery(query);//rs contendrá todos los registros
             
            //zpl();
            rs.next();

            //while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
            //jComboBox_AlbNormal.addItem(rs.getString("NUMALB") + " " + rs.getString("CTE"));
            if (rs.getRow() != 0) {
                System.out.println("entrando..." + "registro: "+rs.getRow() + " " );
                jTextField_AlbCte1.setText(rs.getString("CTE"));
                jTextField_AlbNomCte1.setText(rs.getString("DESTINO"));
                jTextField_AlbNumAlb1.setText(rs.getString("NUMALB"));
                jTextField_AlbPedidoCte1.setText(rs.getString("NUMPED"));
                jTextField_AlbTrans1.setText(rs.getString("TRANSPORTISTA"));
                jTextField_AlbDirEnv1.setText(rs.getString("DIR1"));
                jTextField_AlbBultos1.setText(rs.getString("BULTOS"));
                jTextField_SSCC_pallets.setText(rs.getString("PALLETS"));
                //jTextField_AlbDir2.setText(rs.getString("DIR2"));
                jTextField_AlbDirPostal1.setText(rs.getString("CP") + " " + rs.getString("CITY") + " " + rs.getString("PROVINCIA"));
                jTextField_AlbPrep1.setText(rs.getString("PREPARADOR"));
                //zpl(10,Integer.parseInt(jTextField_AlbBultos1.getText()),0,"");
                //zpl(10,1,0,"");
                System.out.println("salgo...");
            }
            else{
                
                JOptionPane.showMessageDialog(this, "no existe registro para ese albarán");
            }

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
            //zpl();
            //}
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
         // AQUÍ PARA RELLENAR SSCC 
        try {
                String query = "SELECT DEL.SDHNUM_0 AS NUM_ALB, CAB.CPY_0 AS PLANTA, CAB.SCCCOD_0 AS COD_SSCC\n" +
                            ", JOU.LOT_0 AS LOTE\n" +
                            ", ITM.ITMREF_0, ITM.ITMDES1_0 AS ITM_DESC, ITM.YEANCOD14_0 AS GTIN\n" +
                            ", DEL.YPACK_0 / DEL.PACNBR_0 AS CAJAS\n" +
                            "FROM LIVE.SDELIVERY DEL\n" +
                            "INNER JOIN LIVE.SPACK CAB ON CAB.VCRNUM_0 = DEL.SDHNUM_0\n" +
                            "INNER JOIN LIVE.SPACKD PAC ON PAC.VCRNUM_0 = DEL.SDHNUM_0 AND PAC.PACNUM_0 = CAB.PACNUM_0\n" +
                            "INNER JOIN LIVE.STOJOU JOU ON JOU.VCRNUM_0 = DEL.SDHNUM_0 AND JOU.VCRLIN_0 = PAC.VCRLIN_0\n" +
                            "INNER JOIN LIVE.ITMMASTER ITM ON ITM.ITMREF_0 = JOU.ITMREF_0 \n" +
                            "WHERE DEL.SDHNUM_0='" + jTextField_AlbNumAlb1.getText() + "'" + "\n" +
                            "GROUP BY DEL.SDHNUM_0, CAB.CPY_0, CAB.SCCCOD_0, JOU.LOT_0, ITM.ITMREF_0, ITM.ITMDES1_0, ITM.YEANCOD14_0, DEL.YPACK_0 / DEL.PACNBR_0, CAB.PACNUM_0\n" +
                            "ORDER BY CAB.PACNUM_0" ;
                    
              
                                                 
             rs = stmt.executeQuery(query);//rs contendrá todos los registros
             //zpl();
             //rs.next();
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                if (rs.getRow() != 0) {
                    System.out.println("entrando..." + "registro SSCC: "+rs.getRow() + " " );
                    //jTextField_AlbCte1.setText(rs.getString("COD_SSCC"));
                    System.out.println(rs.getString("COD_SSCC"));
                   //jTextField_AlbDir2.setText(rs.getString("DIR2"));
                   // jTextField_AlbDirPostal1.setText(rs.getString("CP") + " " + rs.getString("CITY") + " " + rs.getString("PROVINCIA"));
                   //zpl(10,Integer.parseInt(jTextField_AlbBultos1.getText()),0,"");
                    zpl(10,1,0,"");
                    System.out.println("salgo SSCC...");
                }

                else{

                    JOptionPane.showMessageDialog(this, "no existe empaquetado para ese albarán");
                }
            }
           
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
    }
            
    private void jCombo_refMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCombo_refMouseClicked
        // TODO add your handling code here:
             
    }//GEN-LAST:event_jCombo_refMouseClicked

    private void jCombo_refActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCombo_refActionPerformed
        // TODO add your handling code here:
  
    }//GEN-LAST:event_jCombo_refActionPerformed

    private int pos_espacio (String pCadena){
        //devuelve posición del espacio 
        return pCadena.indexOf(" ");
    }
    
    private int pos_punto (String pCadena){
        return pCadena.indexOf(".");
    }
    private void jCombo_refItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCombo_refItemStateChanged

    }//GEN-LAST:event_jCombo_refItemStateChanged

    private void jDialog1WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog1WindowClosed
        // cierro la conexión cuando se cierra la ventana jDialog1
        if (con != null) {

            try {

                con.close();
                stmt.close();
                rs.close();
               

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(null, ex);

            }

        }

             
    }//GEN-LAST:event_jDialog1WindowClosed

    private void jFormattedTextField_cantidadFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_cantidadFocusLost
        // validar valor del número de etiquetas a imprimir
        
    }//GEN-LAST:event_jFormattedTextField_cantidadFocusLost

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // limpio el textArea de la pantalla de registro
        jTextArea2.setText(null);
        
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // generar etiqueta
        generar_fichero (2);
        ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jCombo_pedproItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCombo_pedproItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_pedproItemStateChanged

    private void jCombo_pedproMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCombo_pedproMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_pedproMouseClicked

    private void jCombo_pedproActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCombo_pedproActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_pedproActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
             //mostramos la ventana jdialog2
        
        DoDesconnect();
        DoConnect();
        jDialog2.setVisible(true);
        
        //LLenamos nuestro ComboBox con los pedidos a proveedor
           
        try {
           
            String query = "select PEDPRO_NUMPED, PEDPRO_PROV, PEDPRO_RAZON_SOCIAL From V_PEDIDOS_PROVEEDOR_ART_NEW GROUP BY PEDPRO_NUMPED, PEDPRO_PROV, PEDPRO_RAZON_SOCIAL";

            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();
            
            while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en los campos
                jCombo_pedpro.addItem(rs.getString("PEDPRO_NUMPED") + " " + rs.getString("PEDPRO_PROV") + " " + rs.getString("PEDPRO_RAZON_SOCIAL"));
                
            }
            
                

          
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
       
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jDialog2WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog2WindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_jDialog2WindowClosed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        //se imprimen tantas etiquetas EAN como unidades tenga el grid y tantas DUN14 como unidades/capacidad dun
        // relleno textarea ZPL
        jTextArea3.setText(null); //limpio el textArea3
        for (int i = 0; i < jTb.getRowCount(); i++) {
            System.out.println("Registro número: " + i);
            System.out.println("Art: " + jTb.getValueAt(i, 0));
            System.out.println("unidades: " + jTb.getValueAt(i, 1));
            System.out.println("dun14: " + jTb.getValueAt(i, 2));
            System.out.println("capacidad: " + jTb.getValueAt(i, 3));
            System.out.println("ean: " + jTb.getValueAt(i, 4));
            System.out.println("info lote: " + jTb.getValueAt(i, 5));
            System.out.println("desc art: " + jTb.getValueAt(i, 6));
            System.out.println("seleccionado: " + jTb.getValueAt(i, 7));

            if (jTb.getRowCount() > 0) {
                sel = jTb.getSelectedRow();
                System.out.println("entro en: " + sel);
            }

            //si la línea está seleccionada, la imprimo
            if ((boolean) jTb.getValueAt(i, 7)) {
                System.out.println("linea seleccionada");
                String sCantidad = (String) model.getValueAt(jTb.getSelectedRow(), 1); //paso el valor de la columna unidades para contador de impresión
                int cantidad = Integer.parseInt(sCantidad);
                String sCapacidad_dun = (String) model.getValueAt(jTb.getSelectedRow(), 3); //paso el valor de las unidades que van en cada embalaje
                int capacidad_dun = Integer.parseInt(sCapacidad_dun);
                System.out.println("cantidad a imprimir: " + cantidad);
               
                if (jTb.getValueAt(i, 4)==null) { //sin ean asignado al artículo no imprimo la etiqueta
                    System.out.println("001");
                    JOptionPane.showMessageDialog(this, "Referencia: " + jTb.getValueAt(i, 0) + " sin EAN asignado");
                } else {
                    zpl(3, cantidad, capacidad_dun,"");
                }
            }

        }
        generar_fichero(3);
        ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        


    }//GEN-LAST:event_jButton5ActionPerformed

    private void jText_pedproNumpedKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jText_pedproNumpedKeyTyped
        // capturo la entrada de enter para lanzar consulta
                       
        if ((evt.getKeyChar()=='\n')) {
            Toolkit.getDefaultToolkit().beep();
            System.out.println("ENTER pressed");
            consultar_pedpro();
        }
       
    }//GEN-LAST:event_jText_pedproNumpedKeyTyped

    private void jText_pedproNumpedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jText_pedproNumpedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jText_pedproNumpedActionPerformed

    private void jMenuItem_albaranNormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_albaranNormalActionPerformed
        // Muestro el jDialog3 para los albaranes normales y relleno el combobox con los albaranes
        DoDesconnect();
        DoConnect_MSSQL();
        jDialog3.setVisible(true);
                
         try {

         
             String query = "SELECT TOP 100 DEL.SOHNUM_0 AS PEDIDO_DENOX, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPCORD_0, DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0, \n"
                    + "ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0, VAL2.PREUSR_0 AS PREPARADOR \n"
                    + "fROM LIVE.SDELIVERY DEL\n"
                    + "INNER JOIN LIVE.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN LIVE.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN LIVE.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN LIVE.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN LIVE.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
                    + "WHERE DEL.STOFCY_0 = '" + buttonGroup_alb.getSelection().getActionCommand() + "' AND DEL.SDHNUM_0 NOT LIKE 'ALD" + "%'\n"
                    + "ORDER BY DEL.SDHNUM_0 DESC";
            

            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();

            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_AlbNormal.addItem(rs.getString("NUMALB") + " " + rs.getString("CTE"));
                //jTextField_AlbCte.setText(rs.getString("CTE"));
                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
                //zpl();
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
       
  
        
    }//GEN-LAST:event_jMenuItem_albaranNormalActionPerformed

    private void jComboBox_AlbNormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_AlbNormalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_AlbNormalActionPerformed

    private void jButton_ConsultaAlbNormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ConsultaAlbNormalActionPerformed
        // consulta mediante el botón el albarán del campo jTextField_AlbNumAlb
        consultar_albaran_normal();
    }//GEN-LAST:event_jButton_ConsultaAlbNormalActionPerformed

    private void jFormattedTextField_cantidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_cantidadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_cantidadActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Proceso de impresión de las etiquetas para albaranes normales (sin códigos de barras)
        String tipo_doc = "ALBARAN"; 
        String codigo_doc = jTextField_AlbNumAlb.getText();
        int opcion = 0;      
              
        System.out.println ("reimprimir:" + reimprimir(tipo_doc, codigo_doc));
        if (reimprimir(tipo_doc, codigo_doc) == 1) {
          opcion = JOptionPane.showConfirmDialog(this, "Albarán ya impreso, ¿reimprimir?");
            //0=yes, 1=no, 2=cancel
           System.out.println(opcion);
        }
        
                
        if (opcion == 0) { 
            generar_fichero (4);
            ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        }
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField_AlbBultosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_AlbBultosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_AlbBultosActionPerformed

    private void jButton_RefrescarAlbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RefrescarAlbActionPerformed
        // Refresco los datos del dataset de albaranes normales
     try {

          
           String query = "SELECT TOP 100 DEL.SOHNUM_0 AS PEDIDO_DENOX, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPCORD_0, DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0, \n"
                    + "ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0, VAL2.PREUSR_0 AS PREPARADOR \n"
                    + "fROM LIVE.SDELIVERY DEL\n"
                    + "INNER JOIN LIVE.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN LIVE.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN LIVE.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN LIVE.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN LIVE.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN LIVE.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
                    + "ORDER BY DEL.SDHNUM_0 DESC";
            

            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();

            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_AlbNormal.addItem(rs.getString("NUMALB") + " " + rs.getString("CTE"));
                //jTextField_AlbCte.setText(rs.getString("CTE"));
                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
                //zpl();
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
    }//GEN-LAST:event_jButton_RefrescarAlbActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // Imprimir "Etiqueta tonta"
        jTextArea5.setText(null);
        //le paso la cantidad de etiquetas a imprimir y el tamaño de la fuente
        zpl(5,Integer.parseInt(jFormattedTextField_cantidad1.getText()), Integer.parseInt(jFormattedTextField_fontSize.getText()),""); 
        generar_fichero (5);
        ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jFormattedTextField_cantidad1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_cantidad1FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_cantidad1FocusLost

    private void jFormattedTextField_cantidad1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_cantidad1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_cantidad1ActionPerformed

    private void jDialog4WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog4WindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_jDialog4WindowClosed

    private void jMenuItem_tontaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_tontaActionPerformed
        // TODO add your handling code here:
        jDialog4.setVisible(true);
    }//GEN-LAST:event_jMenuItem_tontaActionPerformed

    private void jFormattedTextField_fontSizeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSizeFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSizeFocusLost

    private void jFormattedTextField_fontSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSizeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSizeActionPerformed

    private void jFormattedTextField_fontSizeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSizeKeyTyped
        // TODO add your handling code here:
        if ((evt.getKeyChar()=='\n')) {
            Toolkit.getDefaultToolkit().beep();
            System.out.println("ENTER pressed");
            jTextArea5.setFont( new java.awt.Font ("SansSerif",0, Integer.parseInt(jFormattedTextField_fontSize.getText())));
        }
    }//GEN-LAST:event_jFormattedTextField_fontSizeKeyTyped

    private void jFormattedTextField_fontSize1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize1FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize1FocusLost

    private void jFormattedTextField_fontSize1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize1ActionPerformed

    private void jFormattedTextField_fontSize1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize1KeyTyped

    private void jFormattedTextField_fontSize2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize2FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize2FocusLost

    private void jFormattedTextField_fontSize2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize2ActionPerformed

    private void jFormattedTextField_fontSize2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize2KeyTyped

    private void jTonta_texto4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTonta_texto4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTonta_texto4ActionPerformed

    private void jComboBox_OFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_OFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_OFActionPerformed

    private void jButton_ConsultaOFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ConsultaOFActionPerformed
        // TODO add your handling code here:
        consultar_of();
    }//GEN-LAST:event_jButton_ConsultaOFActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
          // Imprimir "Etiqueta artículo OF"
        int cantidad = 0;
        int capacidad = 0;
        if (jRadioButton_OF.isSelected()) {//impresión de los datos de la OF
                        if (jRadioButton_Caja.isSelected()){//impresión de caja
                            cantidad = Integer.parseInt(jFormattedTextField_CantCaja.getText());
                            capacidad = Integer.parseInt(jFormattedTextField_CapCaja.getText());
                        }else if (jRadioButton_Pallet.isSelected()){//impresión de pallet
                            cantidad = Integer.parseInt(jFormattedTextField_CantPallet.getText());
                            capacidad = Integer.parseInt(jFormattedTextField_CapPallet.getText());
                                
                        }else//impresión del producto
                            cantidad = Integer.parseInt(jFormattedTextField_CantOF.getText());
                }else//impresión de los datos del padre
                {
                        if (jRadioButton_Caja.isSelected()){//impresión de caja
                            cantidad = Integer.parseInt(jFormattedTextField_CantPadreCaja.getText());
                            capacidad = Integer.parseInt(jFormattedTextField_CapCaja.getText());
                        }else if (jRadioButton_Pallet.isSelected()){//impresión de pallet
                            cantidad = Integer.parseInt(jFormattedTextField_CantPadrePallet.getText());
                            capacidad = Integer.parseInt(jFormattedTextField_CapPallet.getText());
                        }else//impresión del producto
                            cantidad = Integer.parseInt(jFormattedTextField_CantOF.getText());
                }
        jTextArea6.setText(null);
        //zpl(6,Integer.parseInt(jFormattedTextField_CantOF.getText()),0); 
        
        
        zpl(6,cantidad,capacidad,""); 
        //zpl(6,cantidad,Integer.parseInt(jFormattedTextField_CapPallet.getText())); 
        //zpl(6,Integer.parseInt(jFormattedTextField_CantOF.getText()),cantidad);
        generar_fichero (6);
        if (jRadioButton_Zebra1.isSelected()) 
            ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        else if(jRadioButton_Zebra2.isSelected())
            ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt2");
        else if (jRadioButton_Godex1.isSelected())
            ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt3");
        else
            ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt4");
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton_RefrescarAlb1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RefrescarAlb1ActionPerformed
        // TODO add your handling code here:
         try {
            //lanzo proceso que genera registros con las cantidades por cada etiqueta, lo hago así porque es el procedure que empleo para el crystal             
            String query = "EXECUTE LIVE.ZLANZA_ETIENVASE @pOF = '" + jTextField_OF.getText() + "', @lan = 'SPA'";
            rs = stmt.executeQuery(query);
            
            while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en los campos
               System.out.println(rs.getString("NUM_ETI") + " " + rs.getString("CANTIDAD"));
                
            }
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
                               
    }//GEN-LAST:event_jButton_RefrescarAlb1ActionPerformed

    private void jMenuItem_OFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_OFActionPerformed
        // TODO add your handling code here:
           // Muestro el jDialog3 para los albaranes normales y relleno el combobox con los albaranes
        DoDesconnect();
        DoConnect_MSSQL();
        jDialog5.setVisible(true);
        jTextArea_NetUse.setText(null);
        ejecutarCMD("CMD /C net use"); //relleno textArea con los mapeos de las impresoras
                
         try {

             String query = "SELECT TOP 100 CAB.MFGNUM_0 AS NUM_OF\n" 
                     +       "FROM LIVE.MFGHEAD CAB\n" 
                     +       "ORDER BY CAB.MFGNUM_0 DESC";
             
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();

            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_OF.addItem(rs.getString("NUM_OF"));
                //jTextField_AlbCte.setText(rs.getString("CTE"));
                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
                //zpl();
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
        
    }//GEN-LAST:event_jMenuItem_OFActionPerformed

    private void jFormattedTextField_CantOFKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantOFKeyPressed
        // TODO add your handling code here:
     
     //jFormattedTextField_CantOF.setValue(new Integer(jFormattedTextField_CantOF.getText()));
          
     
      
        
    }//GEN-LAST:event_jFormattedTextField_CantOFKeyPressed

    private void jFormattedTextField_CantOFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantOFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantOFActionPerformed

    private void jFormattedTextField_CantPalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantPalletActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantPalletActionPerformed

    private void jFormattedTextField_CantPalletKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantPalletKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantPalletKeyPressed

    private void jFormattedTextField_CantCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantCajaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantCajaActionPerformed

    private void jFormattedTextField_CantCajaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantCajaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantCajaKeyPressed

    private void jFormattedTextField_CantPadreCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantPadreCajaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantPadreCajaActionPerformed

    private void jFormattedTextField_CantPadreCajaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantPadreCajaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantPadreCajaKeyPressed

    private void jFormattedTextField_CantPadrePalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantPadrePalletActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantPadrePalletActionPerformed

    private void jFormattedTextField_CantPadrePalletKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantPadrePalletKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantPadrePalletKeyPressed

    private void jComboBox_PadresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_PadresActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_PadresActionPerformed

    private void jRadioButton_PadreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_PadreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_PadreActionPerformed

    private void jRadioButton_ProductoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jRadioButton_ProductoMouseClicked
                       
    }//GEN-LAST:event_jRadioButton_ProductoMouseClicked

    private void jRadioButton_CajaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jRadioButton_CajaMouseClicked

    }//GEN-LAST:event_jRadioButton_CajaMouseClicked

    private void jRadioButton_PalletMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jRadioButton_PalletMouseClicked

    }//GEN-LAST:event_jRadioButton_PalletMouseClicked

    private void jRadioButton_OFMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jRadioButton_OFMouseClicked
        // TODO add your handling code here:
//        if (jRadioButton_OF.isSelected()){
//            jFormattedTextField_CantPadreCaja.setEnabled(false);
//            jFormattedTextField_CantPadrePallet.setEnabled(false);
//            jFormattedTextField_CantCaja.setEnabled(true);
//            jFormattedTextField_CantPallet.setEnabled(true);
//        }
        
    }//GEN-LAST:event_jRadioButton_OFMouseClicked

    private void jRadioButton_PadreMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jRadioButton_PadreMouseClicked
        // TODO add your handling code here:
//         if (jRadioButton_Padre.isSelected()){
//            jFormattedTextField_CantPadreCaja.setEnabled(true);
//            jFormattedTextField_CantPadrePallet.setEnabled(true);
//            jFormattedTextField_CantCaja.setEnabled(false);
//            jFormattedTextField_CantPallet.setEnabled(false);
//        }
    }//GEN-LAST:event_jRadioButton_PadreMouseClicked

    private void jFormattedTextField_CapPalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CapPalletActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CapPalletActionPerformed

    private void jFormattedTextField_CapPalletKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_CapPalletKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CapPalletKeyPressed

    private void jFormattedTextField_PicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_PicoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_PicoActionPerformed

    private void jFormattedTextField_PicoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_PicoKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_PicoKeyPressed

    private void jFormattedTextField_CapCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CapCajaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CapCajaActionPerformed

    private void jFormattedTextField_CapCajaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_CapCajaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CapCajaKeyPressed

    private void jFormattedTextField_PicoCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_PicoCajaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_PicoCajaActionPerformed

    private void jFormattedTextField_PicoCajaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_PicoCajaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_PicoCajaKeyPressed

    private void jRadioButton_Zebra1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_Zebra1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_Zebra1ActionPerformed

    private void jRadioButton_Zebra2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_Zebra2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_Zebra2ActionPerformed

    private void jRadioButton_Godex1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_Godex1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_Godex1ActionPerformed

    private void jRadioButton_ProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_ProductoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_ProductoActionPerformed

    private void jRadioButton_CajaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_CajaItemStateChanged
        // TODO add your handling code here:
        check_radiobuttons_OF();
        check_radiobutton_tipo();
                
    }//GEN-LAST:event_jRadioButton_CajaItemStateChanged

    private void jRadioButton_ProductoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_ProductoItemStateChanged
        // TODO add your handling code here:
        check_radiobuttons_OF();
        check_radiobutton_tipo();
    }//GEN-LAST:event_jRadioButton_ProductoItemStateChanged

    private void jRadioButton_PalletItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_PalletItemStateChanged
        // TODO add your handling code here:
        check_radiobuttons_OF();
        check_radiobutton_tipo();
    }//GEN-LAST:event_jRadioButton_PalletItemStateChanged

    private void jRadioButton_OFItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_OFItemStateChanged
        // TODO add your handling code here:
        check_radiobuttons_OF();
    }//GEN-LAST:event_jRadioButton_OFItemStateChanged

    private void jRadioButton_PadreItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_PadreItemStateChanged
        // TODO add your handling code here:
        check_radiobuttons_OF();
    }//GEN-LAST:event_jRadioButton_PadreItemStateChanged

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        jTextArea7.setText(null);
        //Impresión de etiquetas de stock para materia prima
                
        try {
           
            String query = "SELECT LOC_0 AS UBI\n"
                      + "FROM LIVE.STOLOC\n"
                      + "WHERE LOCTYP_0 = '" + jComboBox_Ubi.getSelectedItem().toString() + "'\n"
                      + "AND LOC_0 BETWEEN '" + jText_UbiDesde.getText() + "' and '" + jText_UbiHasta.getText() + "'";
            
            System.out.println(query);
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            while (rs.next()) { //muevo el cursor al primer registro y paso el tamaño de fuente y la ubicación a imprimir
                 zpl(7, 0 , Integer.parseInt(jFormattedTextField_fontSize3.getText()), rs.getString("UBI"));
                 System.out.println("voy al zpl 7");
            }            
            

        } catch (SQLException err) {
            if (err.getErrorCode()!= 0) { //La instrucción Insert no devuelve registros, de modo que elimino la alerta por tener el recordset vacío
                JOptionPane.showMessageDialog(this, err.getMessage()+ ' ' + err.getErrorCode());
            }

        }
        generar_fichero (7);
        ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jFormattedTextField_cantidadUbiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_cantidadUbiFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_cantidadUbiFocusLost

    private void jFormattedTextField_cantidadUbiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_cantidadUbiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_cantidadUbiActionPerformed

    private void jFormattedTextField_fontSize3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize3FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize3FocusLost

    private void jFormattedTextField_fontSize3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize3ActionPerformed

    private void jFormattedTextField_fontSize3KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize3KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize3KeyTyped

    private void jDialog6WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog6WindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_jDialog6WindowClosed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
      // Muestro el jDialog6 para las etiquetas de las ubicaciones
        DoDesconnect();
        DoConnect_MSSQLV12();
        jDialog6.setVisible(true);
           
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jRadioButton_UbiTriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_UbiTriItemStateChanged
        // TODO add your handling code here:
      check_radiobutton_ubi();
                         
       
    }//GEN-LAST:event_jRadioButton_UbiTriItemStateChanged

    private void jRadioButton_UbiFamItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_UbiFamItemStateChanged
        // TODO add your handling code here:
        check_radiobutton_ubi();
    }//GEN-LAST:event_jRadioButton_UbiFamItemStateChanged

    private void jComboBox_UbiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jComboBox_UbiMouseClicked
        // TODO add your handling code here:
      
    }//GEN-LAST:event_jComboBox_UbiMouseClicked

    private void jButton_UbiConsultaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_UbiConsultaActionPerformed
        // TODO add your handling code here:
        consultar_tipoUbi();
        contar_Ubicaciones(jText_UbiDesde.getText(), jText_UbiHasta.getText());
    }//GEN-LAST:event_jButton_UbiConsultaActionPerformed

    private void jText_UbiHastaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jText_UbiHastaActionPerformed
        // TODO add your handling code here:
        if (jText_UbiDesde.getText()!="" && jText_UbiHasta.getText()!= ""){
            contar_Ubicaciones(jText_UbiDesde.getText(), jText_UbiHasta.getText());
        }else
            JOptionPane.showMessageDialog(this, "Rellena los valores..." );
        
        
    }//GEN-LAST:event_jText_UbiHastaActionPerformed

    private void jButton_EtqSimple_GenerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_EtqSimple_GenerarActionPerformed
        // CONSULTAR EtqSimple referencia
       consultar_EtqSimple_ref();
       zpl(8,Integer.parseInt(jFormattedTextField_CantRef.getText()),0,"");
        
    }//GEN-LAST:event_jButton_EtqSimple_GenerarActionPerformed

    private void jCombo_EtqSimple_refItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCombo_EtqSimple_refItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_EtqSimple_refItemStateChanged

    private void jCombo_EtqSimple_refMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCombo_EtqSimple_refMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_EtqSimple_refMouseClicked

    private void jCombo_EtqSimple_refActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCombo_EtqSimple_refActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_EtqSimple_refActionPerformed

    private void jButton_EtqSimple_ImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_EtqSimple_ImprimirActionPerformed
        // TODO add your handling code here:
        generar_fichero (8);
        ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        
    }//GEN-LAST:event_jButton_EtqSimple_ImprimirActionPerformed

    private void jFormattedTextField_CantRefFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantRefFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantRefFocusLost

    private void jFormattedTextField_CantRefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantRefActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantRefActionPerformed

    private void jDialog7WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog7WindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_jDialog7WindowClosed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        //mostramos la ventana jdialog7 (Etiqueta ref. simple)
        
        DoDesconnect();
        DoConnect_MSSQL();
        jDialog7.setVisible(true);
        
        //LLenamos nuestro ComboBox
           
        try {
            
            String query = "SELECT ITM.ITMREF_0 AS REF, ATX.LANGUE_0 AS LAN, ITM.ZITMREFSC_0 AS REFSC, ITM.EANCOD_0 AS EAN, ATX.TEXTE_0 AS TEXTO FROM \n" +
                            "LIVE.ITMMASTER ITM \n" +
                            "INNER JOIN LIVE.ATEXTRA ATX ON ATX.IDENT1_0 = ITM.ITMREF_0\n" +
                            "WHERE ATX.CODFIC_0 = 'ITMMASTER'\n" +
                            "AND ATX.ZONE_0 = 'DES1AXX' ORDER BY ITM.ITMREF_0, ATX.LANGUE_0"; 
                            //"AND ATX.LANGUE_0 = 'SPA'";
            
            //String query = "select REFERENCIA, CLIENTE, NOM_CTE, DESCRIP, EAN FROM V_ARTI_DUN14_NEW";

            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();
            
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                jCombo_EtqSimple_ref.addItem(rs.getString("REF") + " " + rs.getString("LAN"));

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
                //zpl();
            }
            
                

          
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
       
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jButton_ElCorte_PrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ElCorte_PrintActionPerformed
        // TODO add your handling code here:
        if (jTextField_ElCorte_UdCaja.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(this, "Rellena las Uds / Caja: ");
        }
        else {
            //consultar_ElCorte();
            generar_fichero(9);
            ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        }
        
    }//GEN-LAST:event_jButton_ElCorte_PrintActionPerformed

    private void jButton_ElCorte_GenerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ElCorte_GenerarActionPerformed
        // TODO add your handling code here:
        consultar_ElCorte();
        zpl(9,Integer.parseInt(jFormattedTextField_CantElCorte.getText()),0,"");
    }//GEN-LAST:event_jButton_ElCorte_GenerarActionPerformed

    private void jCombo_ElCorteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCombo_ElCorteItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_ElCorteItemStateChanged

    private void jCombo_ElCorteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCombo_ElCorteMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_ElCorteMouseClicked

    private void jCombo_ElCorteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCombo_ElCorteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCombo_ElCorteActionPerformed

    private void jFormattedTextField_CantElCorteFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantElCorteFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantElCorteFocusLost

    private void jFormattedTextField_CantElCorteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_CantElCorteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_CantElCorteActionPerformed

    private void jDialog8WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog8WindowClosed
        // TODO add your handling code here:
        System.out.println("Saliendo de jDialog8");
        DoDesconnect();
    }//GEN-LAST:event_jDialog8WindowClosed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        // TODO add your handling code here:
        DoDesconnect();
        DoConnect_MSSQL();
        jDialog8.setVisible(true);
        rellenar_ElCorte();
        
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jTextField_ElCorte_ArtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_ElCorte_ArtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_ElCorte_ArtActionPerformed

    private void jTextField_ElCorte_UdCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_ElCorte_UdCajaActionPerformed
        // TODO add your handling code here:
          if (jTextField_ElCorte_UdCaja.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(this, "Rellena las Uds / Caja: ");
            jCombo_ElCorte.setEnabled(false);
        }
        else
            jCombo_ElCorte.setEnabled(true);
    }//GEN-LAST:event_jTextField_ElCorte_UdCajaActionPerformed

    private void jTextField_ElCorte_UdCajaPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTextField_ElCorte_UdCajaPropertyChange
        // TODO add your handling code here:
        
      
    }//GEN-LAST:event_jTextField_ElCorte_UdCajaPropertyChange

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jFormattedTextField_fontSize4FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize4FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize4FocusLost

    private void jFormattedTextField_fontSize4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize4ActionPerformed

    private void jFormattedTextField_fontSize4KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_fontSize4KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_fontSize4KeyTyped

    private void jRadioButton_PlantaFItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_PlantaFItemStateChanged
        // TODO add your handling code here:
        check_radiobutton_stocks();
    }//GEN-LAST:event_jRadioButton_PlantaFItemStateChanged

    private void jRadioButton_PlantaTItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_PlantaTItemStateChanged
        // TODO add your handling code here:
        check_radiobutton_stocks();
    }//GEN-LAST:event_jRadioButton_PlantaTItemStateChanged

    private void jComboBox_StockMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jComboBox_StockMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_StockMouseClicked

    private void jButton_ItmLotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ItmLotActionPerformed
        // TODO add your handling code here:
        consultar_StockLotes();
    }//GEN-LAST:event_jButton_ItmLotActionPerformed

    private void jText_StockHastaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jText_StockHastaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jText_StockHastaActionPerformed

    private void jFormattedTextField_cantidadUbi1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFormattedTextField_cantidadUbi1FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_cantidadUbi1FocusLost

    private void jFormattedTextField_cantidadUbi1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField_cantidadUbi1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFormattedTextField_cantidadUbi1ActionPerformed

    private void jDialog9WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog9WindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_jDialog9WindowClosed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        // TODO add your handling code here:
        DoDesconnect();
        DoConnect_MSSQLV12();
        jDialog9.setVisible(true);
        
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jRadioButton_alb_triActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_alb_triActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_alb_triActionPerformed

    private void jRadioButton_UbiFamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_UbiFamActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_UbiFamActionPerformed

    private void jRadioButton_alb_famItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_alb_famItemStateChanged
        // TODO add your handling code here:
        System.out.println("rellenar albaran normal");
        rellenar_albaran_normal();
    }//GEN-LAST:event_jRadioButton_alb_famItemStateChanged

    private void jText_alb_dascherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jText_alb_dascherActionPerformed
        // TODO add your handling code here:
        //si relleno el albarán aplico el filtro por el albarán introducido
        if (jText_alb_dascher.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(this, "Rellena el albarán!");
            
        }
        else
            consultar_alb_dascher(0);
    }//GEN-LAST:event_jText_alb_dascherActionPerformed

    private void jText_alb_dascherKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jText_alb_dascherKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jText_alb_dascherKeyTyped

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        //Generación del fichero para subir a dascher
        boolean send_xpo = false;
        jTextArea11.setText(null); //limpio el textArea11
        for (int i = 0; i < jTb_dascher.getRowCount(); i++) {
           //sel = jTb_dascher.getSelectedRow();
           //System.out.println("entro en: " + sel);
           //si la línea está seleccionada, la genero en el fichero
            if ((boolean) jTb_dascher.getValueAt(i, 12)) {
                System.out.println("Registro número: " + i);
                System.out.println("Alb: " + jTb_dascher.getValueAt(i, 1));
                System.out.println("Consig: " + jTb_dascher.getValueAt(i, 2));
                System.out.println("Dir: " + jTb_dascher.getValueAt(i, 3));
                System.out.println("CP: " + jTb_dascher.getValueAt(i, 4));
                System.out.println("Pob: " + jTb_dascher.getValueAt(i, 5));
                System.out.println("Pais: " + jTb_dascher.getValueAt(i, 6));
                System.out.println("Bultos: " + jTb_dascher.getValueAt(i, 7));
                System.out.println("Peso: " + jTb_dascher.getValueAt(i, 8));
                System.out.println("Portes: " + jTb_dascher.getValueAt(i, 9));
                System.out.println("Mail: " + jTb_dascher.getValueAt(i, 10));
                System.out.println("Tfno: " + jTb_dascher.getValueAt(i, 11));
                System.out.println("Seleccionado: " + jTb_dascher.getValueAt(i, 12));
                System.out.println("enviado: " + jTb_dascher.getValueAt(i, 13));
                System.out.println("Fecha entrega: " + jTb_dascher.getValueAt(i, 14));
                System.out.println("Obs1: " + jTb_dascher.getValueAt(i, 15));
                System.out.println("Obs2: " + jTb_dascher.getValueAt(i, 16));
                System.out.println("linea seleccionada");
                
            
               
                if (jTb_dascher.getValueAt(i, 13)==null) { //si no hay anterior envío lo ejecuto
                    
                     if (jRadioButton_DASCHER.isSelected()){
                               generar_fichero_dascher(i);
                          }
                     else if   (jRadioButton_XPO.isSelected()){
                                generar_fichero_xpo(i); 
                                send_xpo = true;
                                //ejecutar_sendftp("CMD /C C:\\XPO\\send.bat");
                          }
                     else if   (jRadioButton_MRW.isSelected()){
                                generar_fichero_mrw(i); 
                                //ejecutar_sendftp("CMD /C C:\\MRW\\send.bat");
                          }
                } else {//si ya ha sido enviado pregunto para volver a enviar
                    int opcion = 0;
                    opcion = JOptionPane.showConfirmDialog(this, "Albarán: " + jTb_dascher.getValueAt(i, 1) + " ya enviado, ¿reenviar?");
                    //0=yes, 1=no, 2=cancel
                    if (opcion == 0){
                          if (jRadioButton_DASCHER.isSelected()){
                               generar_fichero_dascher(i);
                          }
                          else if   (jRadioButton_XPO.isSelected()){
                                generar_fichero_xpo(i); 
                                send_xpo = true;
                                //ejecutar_sendftp("CMD /C C:\\XPO\\send.bat");
                          }
                          else if   (jRadioButton_MRW.isSelected()){
                                generar_fichero_mrw(i); 
                                //ejecutar_sendftp("CMD /C C:\\MRW\\send.bat");
                          }
                    }
                    System.out.println(opcion);
                }                

            }

        }
       
        generar_fichero(11);
        if (send_xpo){ //para no realizar el envío tantas veces como líneas de albarán generadas
          ejecutar_sendftp("CMD /C C:\\XPO\\send.bat");  
            //System.out.println("Ejecutando send.bat para xpo");
        }
        consultar_alb_dascher(1);//relleno de nuevo la tabla para tener datos actualizados del último envío
        //ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
        
    }//GEN-LAST:event_jButton10ActionPerformed

    private void generar_fichero_dascher(int pLinea){
        //Voy metiendo los espacios en blanco para posicionar los datos donde corresponde según el documento de Dascher
                
                String sRemite = (String) model.getValueAt(pLinea, 0);
                //sRemite = String.format("%20s", ' ');
                jTextArea11.append(sRemite);
                
                String sNumalb = (String) model.getValueAt(pLinea, 1);
                Integer vCad = 25 - sNumalb.length();
                //sNumalb =  sNumalb + String.format("%25s",' ');
                sNumalb =  sNumalb + String.format("%"+vCad+"s",' ');
                jTextArea11.append(sNumalb);
                System.out.println('1');
                
                String sNomConsig = (String) model.getValueAt(pLinea, 2);
                vCad = 32 - sNomConsig.length();
                if (vCad > 0){
                    sNomConsig =  sNomConsig + String.format("%"+vCad+"s",' ');
                    System.out.println('2');
                }
                jTextArea11.append(sNomConsig);
                                
                String sDirConsig = (String) model.getValueAt(pLinea, 3);
                vCad = 32 - sDirConsig.length();
                if (vCad > 0){
                    sDirConsig =  sDirConsig + String.format("%"+vCad+"s",' ');
                    System.out.println('3');
                }
                jTextArea11.append(sDirConsig);
                                               
                String sCP = (String) model.getValueAt(pLinea, 4);
                vCad = 12 - sCP.length();
                if (vCad > 0){
                    sCP =  sCP + String.format("%"+vCad+"s",' ');
                    System.out.println('4');
                }
                jTextArea11.append(sCP);
                
                String sPobConsig = (String) model.getValueAt(pLinea, 5);
                vCad = 40 - sPobConsig.length();
                if (vCad >0){
                    sPobConsig =  sPobConsig + String.format("%"+vCad+"s",' ');
                    System.out.println('5');
                }
                jTextArea11.append(sPobConsig);
                
                String sPaisConsig = (String) model.getValueAt(pLinea, 6);
                vCad = 2 - sPaisConsig.length();
                if (vCad > 0){
                    sPaisConsig =  sPaisConsig + String.format("%"+vCad+"s",' ');
                    System.out.println('6');
                }
                jTextArea11.append(sPaisConsig);
                                
                System.out.println('6');
                String sBultos = (String) model.getValueAt(pLinea, 7);
                jTextArea11.append(sBultos);
                
                String sPeso = (String) model.getValueAt(pLinea, 8);
                jTextArea11.append(sPeso);
                System.out.println('7');
                
                String sPortes = (String) model.getValueAt(pLinea, 9);
                //sPortes =  sPortes + String.format("%102s",' ');
                sPortes =  sPortes + String.format("%9s",' ');
                jTextArea11.append(sPortes);
                System.out.println('8');
                
                
                String sObs1 = (String) model.getValueAt (pLinea, 15);
                if (sObs1 == null){ //si no hay texto relleno con caracteres blancos
                    sObs1 = "" + String.format("%38s",' ');
                }else{
                    vCad = 38 - sObs1.length();
                if (vCad > 0){ //si hay texto pinto los 38 ó relleno con blancos hasta 38
                    sObs1 = sObs1 + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(sObs1);
                System.out.println('9');
                
                String sObs2 = (String) model.getValueAt (pLinea, 16);
                if (sObs2 == null){
                    sObs2 = "" + String.format("%38s",' ');
                    System.out.println("9.1" + sObs2);
                    
                }else{
                    vCad = 38 - sObs2.length();
                    if (vCad > 0){
                        sObs2 = sObs2 + String.format("%"+vCad+"s",' ');
                     }
                }
                jTextArea11.append(sObs2);    
                System.out.println("10");
                
                String volumen = (String) model.getValueAt (pLinea, 19);
                if (volumen == null){ //si no hay texto relleno con caracteres blancos
                    volumen = "" + String.format("%6",' ');
                }else{
                    vCad = 6 - volumen.length();
                if (vCad > 0){ //si hay texto pinto los 56 ó relleno con blancos hasta 56
                    volumen = volumen + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(volumen);
                
                Date  sFec_entrega = (Date) model.getValueAt(pLinea, 14);
                String newFec = new SimpleDateFormat("yyyyMMdd").format(sFec_entrega);
                newFec = String.format("%-3s",' ') + newFec;
                jTextArea11.append (newFec);
                System.out.println("11");
                                            
                               
                String sMailConsig = (String) model.getValueAt(pLinea, 10);
                if (sMailConsig == null){
                    sMailConsig = "" + String.format("%80s", ' ');
                }else{
                vCad = 80 - sMailConsig.length();
                if (vCad >0){
                    sMailConsig =  sMailConsig + String.format("%"+vCad+"s",' ');
                   System.out.println("13");
                    }
                }
                jTextArea11.append(sMailConsig);
                              
                String sTfnoConsig = (String) model.getValueAt (pLinea, 11);
                if (sTfnoConsig == null){ //si no hay texto relleno con caracteres blancos
                    sTfnoConsig = "" + String.format("%56s",' ');
                }else{
                    vCad = 56 - sTfnoConsig.length();
                if (vCad > 0){ //si hay texto pinto los 15 ó relleno con blancos hasta 15
                    sTfnoConsig = sTfnoConsig + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(sTfnoConsig);
                
                
                String dir2 = (String) model.getValueAt (pLinea, 17);
                if (dir2 == null){ //si no hay texto relleno con caracteres blancos
                    dir2 = "" + String.format("%30",' ');
                }else{
                    vCad = 30 - dir2.length();
                if (vCad > 0){ //si hay texto pinto los 56 ó relleno con blancos hasta 56
                    dir2 = dir2 + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(dir2);
                
                String obsEtq = (String) model.getValueAt (pLinea, 18);
                if (obsEtq == null){ //si no hay texto relleno con caracteres blancos
                    obsEtq = "" + String.format("%32s",' ');
                }else{
                    vCad = 32 - obsEtq.length();
                if (vCad > 0){ //si hay texto pinto los 56 ó relleno con blancos hasta 56
                    obsEtq = obsEtq + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(obsEtq);
                
                String pedCte = (String) model.getValueAt (pLinea, 20);
                if (pedCte == null){ //si no hay texto relleno con caracteres blancos
                    pedCte = "" + String.format("%25s",' ');
                }else{
                    vCad = 25 - pedCte.length();
                if (vCad > 0){ //si hay texto pinto los 56 ó relleno con blancos hasta 25
                    pedCte = pedCte + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(pedCte + "\n");//añado el fin de línea en último campo
                
                
                 //Añado el albarán al log de impresión para controlar la reimpresión
                log_impresion ("DASCHER", (String) model.getValueAt(pLinea, 1));
    }
    
    private void generar_fichero_xpo(int pLinea){
        //Voy metiendo los espacios en blanco para posicionar los datos donde corresponde según el documento de Dascher
                System.out.println("xpo");
                
                String sRemite = (String) model.getValueAt(pLinea, 0);
                sRemite = String.format("%9s", ' ');
                jTextArea11.append(sRemite);// XPO NO LO NECESITA, DEJO EL ESPACIO PARA QUE LOS SIGUIETNES CAMPOS VAYAN EN SU POSICIÓN CORRECTA
                
                
                String sNumalb = (String) model.getValueAt(pLinea, 1);
                Integer vCad = 25 - sNumalb.length();
                //sNumalb =  sNumalb + String.format("%25s",' ');
                sNumalb =  sNumalb + String.format("%"+vCad+"s",' ');
                jTextArea11.append(sNumalb);
                System.out.println('1');
                
                String sNomConsig = (String) model.getValueAt(pLinea, 2);
                vCad = 32 - sNomConsig.length();
                if (vCad > 0){
                    sNomConsig =  sNomConsig + String.format("%"+vCad+"s",' ');
                    System.out.println('2');
                }
                jTextArea11.append(sNomConsig);
                                
//                String sDirConsig = (String) model.getValueAt(pLinea, 3);
//                vCad = 32 - sDirConsig.length();
//                if (vCad > 0){
//                    sDirConsig =  sDirConsig + String.format("%"+vCad+"s",' ');
//                    System.out.println('3');
//                }
//                jTextArea11.append(sDirConsig);
                System.out.println("antes de línea 3");
                String sDirConsig = (String) model.getValueAt(pLinea, 3);
                System.out.println(sDirConsig);
                sDirConsig = sDirConsig  + (String) model.getValueAt (pLinea, 17); //concateno dir1 y dir2 hasta 50 caracteres, a partir de ahí lo meto en pos504
                System.out.println("despues de dir 2 concatenado:"  + sDirConsig);
                vCad = 50 - sDirConsig.length();
                if (vCad > 0){
                    sDirConsig =  sDirConsig + String.format("%"+vCad+"s",' ');
                    System.out.println('3');
                }
                //jTextArea11.append(sDirConsig);
                System.out.println("substring de 50: "  + sDirConsig.substring(1,50));
                jTextArea11.append(sDirConsig.substring(0, 50));
                
                                               
                String sCP = (String) model.getValueAt(pLinea, 4);
                vCad = 12 - sCP.length();
                if (vCad > 0){
                    sCP =  sCP + String.format("%"+vCad+"s",' ');
                    System.out.println('4');
                }
                jTextArea11.append(sCP);
                
                String sPobConsig = (String) model.getValueAt(pLinea, 5);
                vCad = 40 - sPobConsig.length();
                if (vCad >0){
                    sPobConsig =  sPobConsig + String.format("%"+vCad+"s",' ');
                    System.out.println('5');
                }
                jTextArea11.append(sPobConsig);
                
                String sPaisConsig = (String) model.getValueAt(pLinea, 6);
                vCad = 2 - sPaisConsig.length();
                if (vCad > 0){
                    sPaisConsig =  sPaisConsig + String.format("%"+vCad+"s",' ');
                    System.out.println('6');
                }
                jTextArea11.append(sPaisConsig);
                                
                System.out.println('6');
                String sBultos = (String) model.getValueAt(pLinea, 7);
                jTextArea11.append(sBultos);
                
                String sPeso = (String) model.getValueAt(pLinea, 8);
                jTextArea11.append(sPeso);
                System.out.println('7');
                
                //String sPortes = (String) model.getValueAt(pLinea, 9);
                String sPortes = " ";
                //sPortes =  sPortes + String.format("%102s",' ');
                sPortes =  sPortes + String.format("%9s",' ');
                jTextArea11.append(sPortes); //XPO NO LO NECESITA
                System.out.println('8');
                
                
                String sObs1 = (String) model.getValueAt (pLinea, 15);
                if (sObs1 == null){ //si no hay texto relleno con caracteres blancos
                    sObs1 = "" + String.format("%38s",' ');
                }else{
                    vCad = 38 - sObs1.length();
                if (vCad > 0){ //si hay texto pinto los 38 ó relleno con blancos hasta 38
                    sObs1 = sObs1 + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(sObs1);
                System.out.println('9');
                
                //String sObs2 = (String) model.getValueAt (pLinea, 16);
                String sObs2 = null;
                if (sObs2 == null){
                    sObs2 = "" + String.format("%38s",' ');
                    System.out.println("9.1" + sObs2);
                    
                }else{
                    vCad = 38 - sObs2.length();
                    if (vCad > 0){
                        sObs2 = sObs2 + String.format("%"+vCad+"s",' ');
                     }
                }
                jTextArea11.append(sObs2);//    XPO NO LO NECESITA
                System.out.println("10");
                
                String volumen = (String) model.getValueAt (pLinea, 19);
                if (volumen == null){ //si no hay texto relleno con caracteres blancos
                    volumen = "" + String.format("%6",' ');
                }else{
                    vCad = 6 - volumen.length();
                if (vCad > 0){ //si hay texto pinto los 56 ó relleno con blancos hasta 56
                    volumen = volumen + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(volumen);
                
                Date  sFec_entrega = (Date) model.getValueAt(pLinea, 14);
                String newFec = new SimpleDateFormat("yyyyMMdd").format(sFec_entrega);
                newFec = String.format("%-3s",' ') + newFec;
                jTextArea11.append (newFec);
                System.out.println("11");
                
                
               
                
                                
                //String sMailConsig = (String) model.getValueAt(pLinea, 10);
                String sMailConsig = null;
                if (sMailConsig == null){
                    sMailConsig = "" + String.format("%80s", ' ');
                    //sMailConsig = sMailConsig.replace(" ", "X"); //reemplazo espacios en blanco con X para XPO (en java no permite rellenar con caracteres, sólo espacios en blanco
                                      
                }else{
                vCad = 80 - sMailConsig.length();
                if (vCad >0){
                    sMailConsig =  sMailConsig + String.format("%"+vCad+"s",' ');
                   System.out.println("13");
                    }
                }
                jTextArea11.append(sMailConsig); //XPO NO LO NECESITA
                              
                String sTfnoConsig = (String) model.getValueAt (pLinea, 11);
                if (sTfnoConsig == null){ //si no hay texto relleno con X
                    sTfnoConsig = "" + String.format("%56s",' ').replace(' ', 'X');
                    System.out.println("telefono...");
                    
                    
                }else{
                    vCad = 56 - sTfnoConsig.length();
                if (vCad > 0){ //si hay texto pinto los 15 ó relleno con X hasta 15
                    System.out.println("telefono2..." + sTfnoConsig + "...length: "+sTfnoConsig.length());
                    sTfnoConsig = sTfnoConsig.replace(' ', 'X') + String.format("%"+vCad+"s",' ').replace(' ', 'X');
                    }
                }
                jTextArea11.append(sTfnoConsig);
                
                
                String dir2 = (String) model.getValueAt (pLinea, 17); //revisar para pasar siempre en blanco
                if (dir2 == null){ //si no hay texto relleno con caracteres blancos
                    dir2 = "" + String.format("%30",' ');
                }else{
                    vCad = 30 - dir2.length();
                if (vCad > 0){ //si hay texto pinto los 56 ó relleno con blancos hasta 56
                    dir2 = dir2 + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(dir2);
                
                String obsEtq = (String) model.getValueAt (pLinea, 18);
                if (obsEtq == null){ //si no hay texto relleno con caracteres blancos
                    obsEtq = "" + String.format("%32s",' ');
                }else{
                    vCad = 32 - obsEtq.length();
                if (vCad > 0){ //si hay texto pinto los 56 ó relleno con blancos hasta 56
                    obsEtq = obsEtq + String.format("%"+vCad+"s",' ');
                    }
                }
                jTextArea11.append(obsEtq);
                
                String pedCte = (String) model.getValueAt (pLinea, 20);
                if (pedCte == null){ //si no hay texto relleno con caracteres blancos
                    pedCte = "" + String.format("%25s",' ').replace(' ', 'X');
                    System.out.println("entrando....");
                                        
                }else{
                    vCad = 25 - pedCte.length();
                if (vCad > 0){ //si hay texto pinto los 56 ó relleno con blancos hasta 25
                    pedCte = pedCte + String.format("%"+vCad+"s",' ');
                    System.out.println("entrando222....");
                    //pedCte = String.format("%"+vCad+"s",'H');
                    }
                }
                
                                
                //jTextArea11.append(pedCte + "\n");//añado el fin de línea en último campo
                jTextArea11.append(pedCte);
                
                vCad = 50 - sDirConsig.substring(50).length();
                System.out.println("vCad en dir2: " + vCad);
                //sDirConsig = sDirConsig.substring(50, vCad + 50) + String.format("%"+vCad+"s",' ');
                sDirConsig = sDirConsig.substring(50) + String.format("%"+vCad+"s",' ');
                System.out.println(sDirConsig);
                System.out.println("pinto fin de línea");
                jTextArea11.append(sDirConsig + "\n");
                
                
                 //Añado el albarán al log de impresión para controlar la reimpresión
                log_impresion ("XPO", (String) model.getValueAt(pLinea, 1));
    }
    
     private void generar_fichero_mrw(int pLinea){
        
                System.out.println ("generando fichero MRW");
                String alb_MRW = "", v_separador = ";"; //nº de albarán MRW qaue se deja en blanco ya que se asigna automaticamente cuando se envíe el fichero
                char v_quote = '"'; 
                alb_MRW = v_quote + alb_MRW + v_quote + v_separador;
                jTextArea11.append(alb_MRW);
                //String sRemite = (String) model.getValueAt(pLinea, 0);
                //sRemite = String.format("%20s", ' ');
                //jTextArea11.append(sRemite);
                
                String sNumalb = (String) model.getValueAt(pLinea, 1); //referencia de envío según documento MRW
                String sPedCte = (String) model.getValueAt(pLinea, 20);
                sNumalb = v_quote + sPedCte.substring(0, 13) + " " + sNumalb + v_quote + v_separador;//pongo la OC del cliente delante del albarán a petición de Marta 20231218
                
                
                //Integer vCad = 25 - sNumalb.length();
                //sNumalb =  sNumalb + String.format("%25s",' ');
                //sNumalb =  sNumalb + String.format("%"+vCad+"s",' ');
                jTextArea11.append(sNumalb);
                //System.out.println('1');
                
                String sRefBulto  = "";
                sRefBulto = v_quote + sRefBulto + v_quote + v_separador;
                jTextArea11.append(sRefBulto);
                
                String sPeso = (String) model.getValueAt(pLinea, 8);
                int sPesoInt = Integer.parseInt(sPeso);
                
                sPeso = String.valueOf(sPesoInt);
                sPeso = v_quote + sPeso + v_quote + v_separador;
                jTextArea11.append(sPeso);
                
                String sBultos = (String) model.getValueAt(pLinea, 21);
                int sBultosInt = Integer.parseInt(sBultos);
                sBultos = String.valueOf(sBultosInt);
                
                sBultos = v_quote + sBultos + v_quote + v_separador;
                jTextArea11.append(sBultos);
                
                String sFechaRecogida = "";
                DateFormat fecha = new SimpleDateFormat ("yyyyMMdd");
       
                System.out.println("fecha: " + fecha.format(new Date()));
                String fechaCadena = fecha.format(new Date());
                
                sFechaRecogida = v_quote + fechaCadena + v_quote + v_separador;
                jTextArea11.append (sFechaRecogida);
                
                
                String sObs1 = (String) model.getValueAt (pLinea, 15); //observaciones de entrega
                if (sObs1 == null){
                    sObs1 = "";
                }
                sObs1 =  v_quote + sObs1 + v_quote + v_separador;
                
                jTextArea11.append(sObs1);
                
                String sNomConsig = (String) model.getValueAt(pLinea, 2);
                sNomConsig = v_quote + sNomConsig + v_quote + v_separador;
                jTextArea11.append(sNomConsig);
                                
                String sDirConsig = (String) model.getValueAt(pLinea, 3);
                String sDirConsig2 = (String) model.getValueAt (pLinea, 17);
                System.out.println("dir2: " + sDirConsig2);
                sDirConsig = v_quote + sDirConsig + sDirConsig2 + v_quote + v_separador;
                jTextArea11.append(sDirConsig);
                                               
                String sCP = (String) model.getValueAt(pLinea, 4);
                sCP = v_quote + sCP + v_quote + v_separador;
                jTextArea11.append(sCP);
                
                String sPobConsig = (String) model.getValueAt(pLinea, 5);
                sPobConsig = v_quote + sPobConsig + v_quote + v_separador;
                jTextArea11.append(sPobConsig);
                
                String sPaisConsig = (String) model.getValueAt(pLinea, 6);
                sPaisConsig = v_quote + sPaisConsig + v_quote + v_separador;
                jTextArea11.append(sPaisConsig);
                
                String sTfnoConsig = (String) model.getValueAt (pLinea, 11);
                sTfnoConsig = v_quote + sTfnoConsig + v_quote + v_separador;
                jTextArea11.append(sTfnoConsig);
                
                String sFranquicia = "";
                sFranquicia = v_quote + sFranquicia + v_quote + v_separador;
                //jTextArea11.append (sFranquicia + "\n");//añado el fin de línea en último campo
                jTextArea11.append (sFranquicia);
                
                String sAdicional = "#TipoServicio=0205#";
                
                String cCPortugal = "07";//Los CP de Portual comienzan por 07
                System.out.println("cp portugal:"+ cCPortugal.substring(0, 2));
                if (sCP.substring(1, 3).equals(cCPortugal.substring(0, 2))){//para baleares el tipo de servicio es 0370
                    sAdicional = "#TipoServicio=0370#";
                }
                
                if (model.getValueAt(pLinea, 6).equals("PT")){
                    //sAdicional = "#TipoServicio=0220#"; //para envíos a Portugal es 0220
                    sAdicional = "#TipoServicio=0205#"; //según mrw hay q enviar este.. no lo entiendo pero lo pongo
                }
                                
                
                sAdicional = v_quote + sAdicional + v_quote;
                jTextArea11.append (sAdicional + "\n"); //añado el fin de línea en el último campo
                
                                
                System.out.println('6');
                                                          
                //Añado el albarán al log de impresión para controlar la reimpresión
                log_impresion ("MRW", (String) model.getValueAt(pLinea, 1));
    }
   
    private void jDialog10WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog10WindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_jDialog10WindowClosed

    private void jMenuItem_alb_dascherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_alb_dascherActionPerformed
        // TODO add your handling code here:
        DoDesconnect();
        DoConnect_MSSQLV12();
        jDialog10.setVisible(true);
        consultar_alb_dascher(1);
    }//GEN-LAST:event_jMenuItem_alb_dascherActionPerformed

    private void jComboBox_AlbSSCCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_AlbSSCCActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_AlbSSCCActionPerformed

    private void jTextField_AlbBultos1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_AlbBultos1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_AlbBultos1ActionPerformed

    private void jButton_ConsultaAlbNormal1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ConsultaAlbNormal1ActionPerformed
        // TODO add your handling code here:
        consultar_albaran_sscc();
    }//GEN-LAST:event_jButton_ConsultaAlbNormal1ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
        System.out.println("generando fichero SSCC");
        generar_fichero (12);
        ejecutar_sendftp("CMD /C C:\\XPO\\send_zpl.bat");
        //ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton_RefrescarAlb2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RefrescarAlb2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton_RefrescarAlb2ActionPerformed

    private void jRadioButton_alb_fam1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_alb_fam1ItemStateChanged
        // TODO add your handling code here:
        rellenar_albaran_sscc();
    }//GEN-LAST:event_jRadioButton_alb_fam1ItemStateChanged

    private void jRadioButton_alb_tri1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_alb_tri1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_alb_tri1ActionPerformed

    private void jMenuItem_SSCCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SSCCActionPerformed
        // TODO add your handling code here:
        DoDesconnect();
        DoConnect_MSSQLV12();
        jDialog11.setVisible(true);
        rellenar_albaran_sscc();
        //consultar_alb_sscc(1);
    }//GEN-LAST:event_jMenuItem_SSCCActionPerformed

    private void jMenuItem_albaranesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_albaranesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem_albaranesActionPerformed

    private void jRadioButton_XPOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_XPOActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_XPOActionPerformed

    private void jTextField_SSCC_palletsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_SSCC_palletsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_SSCC_palletsActionPerformed

    private void jRadioButton_KetyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton_KetyItemStateChanged
        check_radiobuttons_OF();
        check_radiobutton_tipo();        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_KetyItemStateChanged

    private void jRadioButton_KetyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jRadioButton_KetyMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_KetyMouseClicked

    private void jRadioButton_KetyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_KetyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_KetyActionPerformed

    private void jTextField_OFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_OFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_OFActionPerformed
    
    private void inicio_tabla(){ 
        //Método para configurar el DefaultTableModel de la tabla.
        //Especificamos el tamaño de cada columna
        jTb.getColumnModel().getColumn(0).setPreferredWidth(45);
        jTb.getColumnModel().getColumn(1).setPreferredWidth(25);
        jTb.getColumnModel().getColumn(2).setPreferredWidth(75);
        jTb.getColumnModel().getColumn(3).setPreferredWidth(20);
        jTb.getColumnModel().getColumn(4).setPreferredWidth(70);
        jTb.getColumnModel().getColumn(5).setPreferredWidth(100);
        jTb.getColumnModel().getColumn(6).setPreferredWidth(30);
        jTb.getColumnModel().getColumn(7).setPreferredWidth(50);
        //Indicamos el DefaultTableModel de nuestra tabla
        model = (DefaultTableModel) jTb.getModel();
        //Indicamos el número de filas
        model.setNumRows(0);        
    } 
    
    private void inicio_tabla_alb_dascher(){ 
        //Método para configurar el DefaultTableModel de la tabla.
        //Especificamos el tamaño de cada columna
        System.out.println("creando tabla albarán dascher");
        jTb_dascher.getColumnModel().getColumn(0).setPreferredWidth(10);
        jTb_dascher.getColumnModel().getColumn(1).setPreferredWidth(80);
        jTb_dascher.getColumnModel().getColumn(2).setPreferredWidth(75);
        jTb_dascher.getColumnModel().getColumn(3).setPreferredWidth(100);
        jTb_dascher.getColumnModel().getColumn(4).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(5).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(6).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(8).setPreferredWidth(20);
        jTb_dascher.getColumnModel().getColumn(9).setPreferredWidth(20);
        jTb_dascher.getColumnModel().getColumn(10).setPreferredWidth(50);
        jTb_dascher.getColumnModel().getColumn(11).setPreferredWidth(50);
        jTb_dascher.getColumnModel().getColumn(12).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(13).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(14).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(15).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(16).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(17).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(18).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(19).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(20).setPreferredWidth(30);
        jTb_dascher.getColumnModel().getColumn(21).setPreferredWidth(30);
        
                
        //Indicamos el DefaultTableModel de nuestra tabla
        System.out.println("definimos default model");
        model = (DefaultTableModel) jTb_dascher.getModel();
        //Indicamos el número de filas
        model.setNumRows(0);        
    } 
     public void rellenaTextArea(int p_modo, String p_Texto1, String p_Texto2, String p_Texto3, String p_Texto4, String p_Texto5) {
        //rellena el textarea con los registros devueltos del filtro aplicado

         if (p_modo == 1) {          //relleno el textArea del albarán
             jTextArea1.append(p_Texto1 + " " + p_Texto2 + " " + p_Texto3 + " " + p_Texto4 + " " + p_Texto5);
             jTextArea1.append(System.getProperty("line.separator"));
         } else if (p_modo == 2) {  //relleno el textArea de pedidos a proveedor

             
             jTextArea3.append(padRight(p_Texto1,20) +  padRight(p_Texto2,18) + padRight(p_Texto3,36)+ padRight(p_Texto4,20)+ p_Texto5);
             jTextArea3.append(System.getProperty("line.separator"));

         }else //relleno el textarea del log del albarán
             
             jTextArea_log.append(p_Texto1 + " " + p_Texto2 + " " + p_Texto3 + " " + p_Texto4 + " " + p_Texto5);
             jTextArea_log.append(System.getProperty("line.separator"));

    }
    public void generar_fichero(int p_TextArea){
           // Guardo la info en un fichero  
        String file_name;
        //La app de dascher no me permite cambiar el nombre del fichero que lee así que le asigno el que tiene indicado por defecto
        if (p_TextArea == 11){
             if (jRadioButton_DASCHER.isSelected()){
                               //file_name = "C:/DACHSER/dach.txt";
                                file_name = "\\\\192.168.35.10/public/DACHSER/dach.txt";
                          }
             else if (jRadioButton_XPO.isSelected()){
                                file_name = "C:/XPO/XPO_FILE.txt";                                      
                          }
             else{
                                file_name = "C:/MRW/MRW_FILE.txt";
             }
          //file_name = "C:/DACHSER/dach.txt";  
            //file_name = "C:/tmp/dach.txt";
        }
        else if (p_TextArea == 12){
            file_name = "C:/XPO/etiqueta.txt";
        }
        else{
          file_name = "C:/tmp/etiqueta.txt";
        }
        
        String tipo_doc ="";
        String codigo_doc = "";
       
        try {
            WriteFile data = new WriteFile(file_name, false);
            //data.writeToFile("this is another line of text ñ");
            if (p_TextArea == 1) {
                data.writeToFile(jTextArea1.getText());
                tipo_doc = "TIPO1";
            } else if (p_TextArea == 2){
                data.writeToFile(jTextArea2.getText());
                tipo_doc = "TIPO2";
            }else if (p_TextArea == 4){
                data.writeToFile(jTextArea4.getText());
                tipo_doc = "ALBARAN";
                codigo_doc = jTextField_AlbNumAlb.getText();
            }else if (p_TextArea == 5){
                data.writeToFile(jTextArea5.getText());
                tipo_doc = "TONTA";
            }else if (p_TextArea == 6){
                data.writeToFile(jTextArea6.getText());
                tipo_doc = "OF";      
            }else if (p_TextArea == 7){
                data.writeToFile(jTextArea7.getText());
                tipo_doc = "UBICA";
            }else if (p_TextArea == 8){
                data.writeToFile(jTextArea8.getText());
                tipo_doc = "ETQSIMP";    
            }else if (p_TextArea == 9){
                data.writeToFile(jTextArea9.getText());
                tipo_doc = "ETELCORTE";    
            }else if (p_TextArea == 11){
                //data.writeToFile(jTextArea11.getText().replaceAll("\\r\\n", ""));
                data.writeToFile(jTextArea11.getText());
                tipo_doc = "DASCHER"; 
                //copia_ficheros.copia("C:/tmp/dach", "C:/tmp/dach_copia.bin");
               // Elimino la última línea del fichero de dascher porque al tratarlo en su web cree que hay datos y peta
                RandomAccessFile f = new RandomAccessFile(file_name, "rw");
                long length = f.length() - 1;
                byte b = f.readByte();
                do {                     
                  length -= 1;
                  f.seek(length);
                  b = f.readByte();
                } while(b != 10);
                f.setLength(length+1);
                f.close();
                
            }else if (p_TextArea == 12){
                data.writeToFile(jTextArea12.getText());
                tipo_doc = "SSCC"; 
            }else{
                data.writeToFile(jTextArea3.getText());
                tipo_doc = "TIPO3";
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
       
        //aquí el log-control del albarán
                   
        if (p_TextArea ==4) {
            log_impresion (tipo_doc, codigo_doc);
        }            
        
        JOptionPane.showMessageDialog(this, "fichero: " + file_name + " generado");
    }
    
    public void log_impresion (String p_tipo, String p_codigo){
        //genero un log de impresión de documentos para controlar si las etiquetas han sido impresas o no       
        
        try {
           
            String query = "BEGIN TRANSACTION "
                    + "INSERT INTO LIVE.ZSDELIVERY_PRINT_LOG (CODIGO, TIPO) VALUES ('" + p_codigo + "','" + p_tipo + "')"
                    + "COMMIT";
                                   
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
                        
            

        } catch (SQLException err) {
            if (err.getErrorCode()!= 0) { //La instrucción Insert no devuelve registros, de modo que elimino la alerta por tener el recordset vacío
                JOptionPane.showMessageDialog(this, err.getMessage()+ ' ' + err.getErrorCode());
            }

        }
    }
    
    public int reimprimir (String p_tipo, String p_codigo){
        int valor = 0;
        
        try {
           
            String query = "SELECT CODIGO FROM  LIVE.ZSDELIVERY_PRINT_LOG WHERE CODIGO = '" + p_codigo + "' AND TIPO = '" + p_tipo + "'";
                    
                                   
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            rs.next();//muevo el cursor al primer registro y relleno el combobox
         
                       
            if (rs.getRow() != 0) {  //el recordset contiene registro
                valor = 1;
            }
            else
                valor =  0;
                                   
            

        } catch (SQLException err) {
                          
                JOptionPane.showMessageDialog(this, err.getMessage()+ ' ' + err.getErrorCode());


        }

        return valor;
    }
    public void zpl(int p_modo, int p_num, int p_unidades, String p_codigo){
        //procedimiento ZPL para impresoras compatibles ZPL
        try {
            if (p_modo == 1) {//rellenar etiquetas para albarán
               // while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en los campos
                    int contador;
                    for (contador = 0; contador < p_num; contador++) {
                        String dun14 = rs.getString("COD_DUN14");
                        String ref = rs.getString("REF_SIN_COLOR");
                        String texto = rs.getString("TEXTO");
                        int cap_dun = rs.getInt("CAPACIDAD_DUN");
                        String sCap_dun = Integer.toString(cap_dun);

                        jTextArea1.append("^XA\n");
                        jTextArea1.append("^PON\n");
                        jTextArea1.append("^LH0,0\n");
                        jTextArea1.append("^CI28\n");
                        jTextArea1.append("^PW832\n");
                        jTextArea1.append("^A0N,40,34\n");
                        jTextArea1.append("^FO640,150\n");
                        jTextArea1.append("^FD " + sCap_dun + "PCS^FS\n");
                        jTextArea1.append("^FO100,150\n");
                        jTextArea1.append("^FB500,2,,L,\n");
                        jTextArea1.append("^A0N,40,34\n");
                        jTextArea1.append("^FD " + ref + " " + texto + "^FS\n");
                        jTextArea1.append("^BY5,2.5,200\n");
                        jTextArea1.append("^FO100,225\n");
                        jTextArea1.append("^FB750,1,,C,\n");
                        jTextArea1.append("^BCN,200,Y,N,N,D\n");
                        jTextArea1.append("^FD" + dun14 + "^FS\n");
                        jTextArea1.append("^XZ\n");
                    }
                //}
            } else if (p_modo == 2) { //rellenar etiquetas sueltas
                int contador;
                for (contador = 0; contador < p_num; contador++) {
                    //String cod_ean = rs.getString("EAN");
                    //String cod_ean = jText_ref_ean.getText();

                    jTextArea2.append("^XA\n");
                    jTextArea2.append("^PON\n");
                    jTextArea2.append("^LH0,0\n");
                    jTextArea2.append("^CI28\n");
                    jTextArea2.append("^PW832\n");//Ancho de la ettiqueta ajustada a 10cm
                    jTextArea2.append("^FO50,50\n");
                    jTextArea2.append("^GB700,750,4^FS\n");
                    jTextArea2.append("^FO158,100\n");
                    jTextArea2.append("^A0,128,100\n");
                    jTextArea2.append("^FD " + jText_referencia.getText() + "^FS\n");
                    jTextArea2.append("^FO175,225\n");
                    jTextArea2.append("^A0,32,25\n");
                    jTextArea2.append("^FD " + jText_ref_desc_art.getText() + "^FS\n");
                    jTextArea2.append("^FO310,550\n");
                    jTextArea2.append("^GB175,175,4^FS\n");
                    jTextArea2.append("^FO320,595\n");
                    jTextArea2.append("^A0,128,100\n");
                    jTextArea2.append("^FD " + jText_ref_capacidad.getText() + "^FS\n");
                    jTextArea2.append("^BY3,2.5,200\n");
                    jTextArea2.append("^FO175,275\n");
                    jTextArea2.append("^BCN,200,Y,N,N,D\n");
                    jTextArea2.append("^FD(01)" + jText_ref_ean.getText() + "^FS\n");
                    jTextArea2.append("^XZ\n");
                }
                //para capacidad dun14 < 2 el codebar es EAN y se imprime una etiqueta, en caso contrario es DUN14 y se imprimen N entiquetas (al valor de cap dun14)
            } else if (p_modo == 3){  //rellenar etiquetas para proveedor
                int contador;
                //imprimo las etiquetas EAN (para el artículo)
                for (contador = 0; contador < p_num; contador++) {
                    jTextArea3.append("^XA\n");
                    jTextArea3.append("^PON\n");
                    jTextArea3.append("^LH0,0\n");
                    jTextArea3.append("^CI28\n");
                    jTextArea3.append("^PW832\n");
                    jTextArea3.append("^FO80,50\n");
                    jTextArea3.append("^FB700,3,,C,\n");
                    jTextArea3.append("^A0N,50,50\n");
                    jTextArea3.append("^FD " + jText_empresa.getText() + "\n");
                    jTextArea3.append("^FO80,200\n");
                    jTextArea3.append("^FB700,3,,C,\n");
                    jTextArea3.append("^A0N,68,68\n");
                    jTextArea3.append("^FD " + model.getValueAt(jTb.getSelectedRow(), 6) + "^FS\n");
                    jTextArea3.append("^FO80,450\n");
                    jTextArea3.append("^A0N,50,50\n");
                    //jTextArea3.append("^FD " + model.getValueAt(jTb.getSelectedRow(), 1) + "^FS\n");
                    jTextArea3.append("^FD 1"); //la etiqueta ean es unitaria
                    jTextArea3.append("^FO130,450\n");
                    jTextArea3.append("^A0N,50,50\n");
                    jTextArea3.append("^FD UNIDAD^FS\n");
                    jTextArea3.append("^FO400,450\n");
                    jTextArea3.append("^A0N,50,50\n");
                    //jTextArea3.append("^FD " + "REF: " + model.getValueAt(jTb.getSelectedRow(), 0) + "^FS\n");
                    String ref_sin_color = (String) model.getValueAt(jTb.getSelectedRow(), 0);
                    jTextArea3.append("^FD " + "REF: " + ref_sin_color.substring(0, pos_punto(ref_sin_color)) + "^FS\n"); //elimino la parte del color de la referencia
                    jTextArea3.append("^BY3,2.5,100\n");
                    jTextArea3.append("^FO225,530\n");
                    jTextArea3.append("^B2N,,Y,N\n");
                    jTextArea3.append("^FD " + model.getValueAt(jTb.getSelectedRow(), 4) + "^FS\n");
                    jTextArea3.append("^FB450,1,,C,\n");
                    jTextArea3.append("^FO30,200^A0B,40,40^FD " + model.getValueAt(jTb.getSelectedRow(), 5) + "^FS\n");
                    jTextArea3.append("^XZ\n");

                }
                //imprimo las etiquetas DUN14 (para la caja). Etiquetas para caja = p_unidades / capacidad dun
                if (p_unidades > 1) {
                    System.out.println("Num etiquetas caja: (" + p_unidades + "/" + p_num + ") " + Math.floor(p_num / p_unidades));
                    for (contador = 0; contador < Math.floor(p_num / p_unidades); contador++) {
                        jTextArea3.append("^XA\n");
                        jTextArea3.append("^PON\n");
                        jTextArea3.append("^LH0,0\n");
                        jTextArea3.append("^CI28\n");
                        jTextArea3.append("^PW832\n");
                        jTextArea3.append("^FO80,50\n");
                        jTextArea3.append("^FB700,3,,C,\n");
                        jTextArea3.append("^A0N,50,50\n");
                        jTextArea3.append("^FD " + jText_empresa.getText() + "\n");
                        jTextArea3.append("^FO80,200\n");
                        jTextArea3.append("^FB700,3,,C,\n");
                        jTextArea3.append("^A0N,68,68\n");
                        jTextArea3.append("^FD " + model.getValueAt(jTb.getSelectedRow(), 6) + "^FS\n");
                        jTextArea3.append("^FO80,450\n");
                        jTextArea3.append("^A0N,50,50\n");
                        jTextArea3.append("^FD " + model.getValueAt(jTb.getSelectedRow(), 3) + "^FS\n");
                        jTextArea3.append("^FO130,450\n");
                        jTextArea3.append("^A0N,50,50\n");
                        jTextArea3.append("^FD UNIDADES^FS\n");
                        jTextArea3.append("^FO400,450\n");
                        jTextArea3.append("^A0N,50,50\n");
                        //jTextArea3.append("^FD " + "REF: " + model.getValueAt(jTb.getSelectedRow(), 0) + "^FS\n");
                        String ref_sin_color = (String) model.getValueAt(jTb.getSelectedRow(), 0);
                        jTextArea3.append("^FD " + "REF: " + ref_sin_color.substring(0, pos_punto(ref_sin_color)) + "^FS\n"); //elimino la parte del color de la referencia
                        jTextArea3.append("^BY3,2.5,100\n");
                        jTextArea3.append("^FO225,530\n");
                        jTextArea3.append("^B2N,,Y,N\n");
                        jTextArea3.append("^FD " + model.getValueAt(jTb.getSelectedRow(), 2) + "^FS\n");
                        jTextArea3.append("^FB450,1,,C,\n");
                        jTextArea3.append("^FO30,200^A0B,40,40^FD " + model.getValueAt(jTb.getSelectedRow(), 5) + "^FS\n");
                        jTextArea3.append("^XZ\n");

                    }
                }
            }
            
            else if (p_modo == 4){
                //aquí etiquetas para albarán normal
                 int contador;
                 int bultos = 0;
                 
                for (contador = 0; contador < p_num; contador++) {
                    //String cod_ean = rs.getString("EAN");
                    //String cod_ean = jText_ref_ean.getText();
                    bultos = bultos + 1;
                    jTextArea4.append("^XA\n");
                    jTextArea4.append("^CI28\n"); //juego de caracterse UTF8
                    jTextArea4.append("^PW832\n");
                    jTextArea4.append("^FO20,20^XGE:LOGO_DENOX.PNG,1,1^FS\n");
                    jTextArea4.append("^PON\n");
                    jTextArea4.append("^LH0,0\n");
                    jTextArea4.append("^FO20,150\n");
                    jTextArea4.append("^GB760,600,4^FS\n"); //RECUADRO
                    jTextArea4.append("^FO400,20\n");
                    jTextArea4.append("^FB700,5,,L,\n");//ZONA DE IMPRESIÓN CONN 5 LÍNEAS
                    jTextArea4.append("^A0N,26,\n");
                    jTextArea4.append("^FD FABRICANTES DE MENAJE, S.A \\& Ctra. Nacional 330km. 486 \\& Pol. Ind. Agrinasa, Naves 30-35 \\& 50420 CADRETE (Zaragoza) \\& Tel. 976 126 210 * Fax 976 126 195^FS\n");
                    jTextArea4.append("^FO30,170\n");
                    jTextArea4.append("^A0N,42,32\n");
                    jTextArea4.append("^FDEnvío a / Sent to: " + jTextField_AlbNomCte.getText() +"^FS\n");
                    jTextArea4.append("^FO30,220\n");
                    jTextArea4.append("^A0N,42,32\n");
                    jTextArea4.append("^FD Su pedido / Your order: " +  jTextField_AlbPedidoCte.getText() + "^FS\n");
                    jTextArea4.append("^FO30,270\n");
                    jTextArea4.append("^A0N,42,32\n");
                    jTextArea4.append("^FD Dirección / Address: ^FS\n");
                    jTextArea4.append("^FO150,320\n");
                    jTextArea4.append("^A0N,30,20\n");
                    jTextArea4.append("^FD " + jTextField_AlbDirEnv.getText() + "^FS\n");
                    jTextArea4.append("^FO150,355\n");
                    jTextArea4.append("^A0N,30,20\n");
                    jTextArea4.append("^FD "+ jTextField_AlbDir2.getText() +"^FS\n");
                    jTextArea4.append("^FO150,390\n");
                    jTextArea4.append("^A0N,30,20\n");
                    jTextArea4.append("^FD "+ jTextField_AlbDirPostal.getText() +"^FS\n");
                    jTextArea4.append("^FO30,430\n");
                    jTextArea4.append("^FB700,3,,L,\n");
                    jTextArea4.append("^A0N,42,32\n");
                    jTextArea4.append("^FD Agencia / Shipping company: " + jTextField_AlbTrans.getText() + "^FS\n");
                    jTextArea4.append("^FO30,550\n");
                    jTextArea4.append("^A0N,42,32\n");
                    jTextArea4.append("^FD Bulto / Package: "+ bultos + "/" + jTextField_AlbBultos.getText() + "^FS\n");
                    jTextArea4.append("^FO760,780\n");
                    jTextArea4.append("^A0N,42,32\n");
                    jTextArea4.append("^FD" + jTextField_AlbPrep.getText() + "^FS\n");
                    jTextArea4.append("^XZ\n");
                  
                                                 
                    
                }
            }
            
             else if (p_modo == 5){
                //aquí etiqueta tonta
                 int contador;
                                  
                for (contador = 0; contador < p_num; contador++) {
                 
                    jTextArea5.append("^XA\n");
                    jTextArea5.append("^CI28\n"); //juego de caracterse UTF8
                    jTextArea5.append("^PW832\n");
                    jTextArea5.append("^FO20,20^XGE:LD5.JPG,1,1^FS\n");
                    jTextArea5.append("^PON\n");
                    //jTextArea5.append("^LH0,0\n");
                    //jTextArea4.append("^FO20,150\n");
                    //jTextArea4.append("^GB760,600,4^FS\n"); //RECUADRO
                    jTextArea5.append("^FO50,50\n");
                    jTextArea5.append("^FB720,1,,C\n");//CREO UNA BOX PARA CADA LÍNEA PARA PONER EL TEXTO CENTRADO
                    jTextArea5.append("^A0N," + p_unidades + ",\n");
                    //jTextArea5.append("^FB700,5,,C,\n");//ZONA DE IMPRESIÓN CONN 5 LÍNEAS
                    jTextArea5.append("^FD" + jTonta_texto.getText() + "^FS\n");
                    jTextArea5.append("^FO50,200\n");
                    jTextArea5.append("^FB720,1,,C\n");
                    jTextArea5.append("^A0N," + p_unidades + ",\n");
                    jTextArea5.append("^FD" + jTonta_texto1.getText() + "^FS\n");
                    jTextArea5.append("^FO50,350\n");
                    jTextArea5.append("^FB720,1,,C\n");
                    jTextArea5.append("^A0N," + p_unidades + ",\n");
                    jTextArea5.append("^FD" + jTonta_texto2.getText() + "^FS\n");
                    jTextArea5.append("^FO50,500\n");
                    jTextArea5.append("^FB720,1,,C\n");
                    jTextArea5.append("^A0N," + p_unidades + ",\n");
                    jTextArea5.append("^FD" + jTonta_texto3.getText() + "^FS\n");
                    jTextArea5.append("^FO50,650\n");
                    jTextArea5.append("^FB720,1,,C\n");
                    jTextArea5.append("^A0N," + p_unidades + ",\n");
                    jTextArea5.append("^FD" + jTonta_texto4.getText() + "^FS\n");
                    jTextArea5.append("^XZ\n");
                        
                }
            }
            else if (p_modo == 6){
                //aquí etiqueta de producto 
                //int contador;
                int bulto = 0;
                int contador;
                String referencia;
                int cantxbulto;
                String OF = jTextField_OF.getText();
               
                
                if (jRadioButton_OF.isSelected()){
                    referencia = jTextField_OFarti.getText();
                }else{
                    referencia = jTextField_PadreArti.getText();
                }
                
                String numLote = jTextField_OfLote.getText().replace(" ", "");
                if (numLote.length() == 0)
                    numLote =  "      ";//le asigno espacio al nº de lote para que el codebar sea válido
                                
                if  (jRadioButton_Producto.isSelected()){
                    // con EZPL se manda el número de copias en lugar de imprimir un bloque por etiqueta. Etiqueta de producto por la GODEX
                    jTextArea6.append("^Q30,3\r\n");
                    jTextArea6.append("^W50\r\n");
                    //jTextArea6.append("^H10\r\n");
                    jTextArea6.append("^H8\r\n");
                    jTextArea6.append("^P"+ p_num + "\r\n"); //número de etiquetas a imprimir
                    //jTextArea6.append("^S6\r\n");
                    jTextArea6.append("^S5\r\n");
                    //jTextArea6.append("^AD\r\n");
                    jTextArea6.append("^AT\r\n");
                    jTextArea6.append("^C1\r\n");
                    //jTextArea6.append("^R16\r\n");
                    jTextArea6.append("^R0\r\n");
                    jTextArea6.append("~Q+0\r\n");
                    jTextArea6.append("^O0\r\n");
                    jTextArea6.append("^D0\r\n");
                    //jTextArea6.append("^E12\r\n");
                    jTextArea6.append("^E12\r\n");
                    //jTextArea6.append("~R255\r\n");
                    jTextArea6.append("~R255\r\n");
                    jTextArea6.append("^L\r\n");
                    jTextArea6.append("Dy2-me-dd\r\n");
                    jTextArea6.append("Th:m:s\r\n");
//                    if (jRadioButton_Logo.isSelected()) {
//                               jTextArea6.append("Y24,8,LD6\r\n");//Pinto el logo si se selecciona la opción (almacenado en memoria de la impresora)
//                               jTextArea6.append("Y24,90,GdiWindowTextBox0\r\n");//Pinto el texto de Denox (almacenado en memoria de la impresora)
//                    }
                     if (jRadioButton_Logo.isSelected()) {
                               jTextArea6.append("Y19,144,Image6\r\n");//Pinto el logo si se selecciona la opción (almacenado en memoria de la impresora)
                               //jTextArea6.append("Y24,90,GdiWindowTextBox0\r\n");//Pinto el texto de Denox (almacenado en memoria de la impresora)
                        }
                    //jTextArea6.append("BE,264,24,3,8,80,0,1," + jTextField_OfEan.getText() +"\r\n");
                    jTextArea6.append("Y164,122,WindowText0\r\n");//Pinto el texto de Denox (almacenado en memoria de la impresora)
                    
                    //jTextArea6.append("W48,200,5,2,M0,8,4,18,0\r\n");
                    //jTextArea6.append(referencia+ "LOT"+ numLote+"\r\n");
                    //jTextArea6.append("AB,48,286,1,1,1,0,LOT: "+jTextField_OFarti.getText()+"LOT"+ numLote+"\r\n");
                    
                    //System.out.println("radio button seleccionado: " + buttonGroup_DatosAImp.getSelection().getActionCommand());
                   
                    
                    if (jRadioButton_OF.isSelected()){
                        jTextArea6.append("AB,03,25,1,1,1,0," + jTextField_OFarti.getText()+"\r\n");
                        jTextArea6.append("BE,157,27,2,10,63,0,1," + jTextField_OfEan.getText() +"\r\n");
                    }
                    else if (jRadioButton_Padre.isSelected()){
                        jTextArea6.append("AB,03,25,1,1,1,0," + jTextField_PadreArti.getText()+"\r\n");
                        jTextArea6.append("BE,157,27,2,10,63,0,1," + jTextField_PadreEan.getText() +"\r\n");
                    }
                    
                    jTextArea6.append("AB,03,55,1,1,1,0," + jTextField_OFdescrip.getText()+"\r\n");
                    //jTextArea6.append("AB,03,85,1,1,1,0,LOT: "+jTextField_OFarti.getText()+"LOT"+ numLote+"\r\n");
                    jTextArea6.append("AB,03,85,1,1,1,0,"+jTextField_OfLote.getText() + "\r\n");
                    //jTextArea6.append("AA,272,156,1,1,1,0," + jTextField_OFdescrip.getText()+"\r\n");
                    
                    jTextArea6.append("AB,03,185,1,1,1,0,www.denox.eu\r\n");
                    jTextArea6.append("E\r\n");
                   

                    
                 } else if (jRadioButton_Kety.isSelected()){ //Etiqueta Kety
                    // con EZPL se manda el número de copias en lugar de imprimir un bloque por etiqueta. Etiqueta de producto por la GODEX
                    jTextArea6.append("^Q55,3\r\n");
                    jTextArea6.append("^W55\r\n");
                    jTextArea6.append("^H10\r\n");
                    jTextArea6.append("^P"+ p_num + "\r\n"); //número de etiquetas a imprimir
                    jTextArea6.append("^S4\r\n");
                    jTextArea6.append("^AD\r\n");
                    jTextArea6.append("^C1\r\n");
                    jTextArea6.append("^R0\r\n");
                    jTextArea6.append("~Q+0\r\n");
                    jTextArea6.append("^O0\r\n");
                    jTextArea6.append("^D0\r\n");
                    jTextArea6.append("^E18\r\n");
                    jTextArea6.append("~R255\r\n");
                    jTextArea6.append("^L\r\n");
                    jTextArea6.append("Dy2-me-dd\r\n");
                    jTextArea6.append("Th:m:s\r\n");
                    if (jRadioButton_Logo.isSelected()) {
                               jTextArea6.append("Y24,8,LD6\r\n");//Pinto el logo si se selecciona la opción (almacenado en memoria de la impresora)
                               jTextArea6.append("Y24,90,GdiWindowTextBox0\r\n");//Pinto el texto de Denox (almacenado en memoria de la impresora)
                    }
                    jTextArea6.append("BE,50,314,3,7,68,0,1," + jTextField_OfEan.getText() +"\r\n");
                    //jTextArea6.append("W48,200,5,2,M0,8,4,18,0\r\n");
                    jTextArea6.append("AB,390,307,1,1,0,1E, "+ numLote +"\r\n");
                    jTextArea6.append("AB,70,250,1,2,0,0E, " + jTextField_OFdescrip.getText()+"\r\n");
                    jTextArea6.append("E\r\n");  
                 }
                
                
                    else{  //AQUÍ ETIQUETAS DE CAJAS Y PALLETS. ETIQUETAS POR LAS ZEBRA
                    
                   for (contador = 0; contador < p_num; contador++){
                                            
                        bulto = bulto + 1;
                        if (jRadioButton_Pallet.isSelected()){
                            cantxbulto = Integer.parseInt(jFormattedTextField_CantPallet.getText()); 
                        }else if (jRadioButton_Caja.isSelected()){
                            cantxbulto = Integer.parseInt(jFormattedTextField_CantCaja.getText());
                        }else 
                            cantxbulto = Integer.parseInt(jFormattedTextField_CantOF.getText());
                        
                        jTextArea6.append("^XA\n");
                        jTextArea6.append("^CI28\n");
                        jTextArea6.append("^PW832\n");
                        jTextArea6.append("^PON\n");
                        jTextArea6.append("^LH0,0\n");
                        jTextArea6.append("^PON\n");
                        jTextArea6.append("^FO20,20^XGE:LD5.JPG,1,1^FS\n");//logo de la empresa
                        jTextArea6.append("^A0N,36,24\n");
                        jTextArea6.append("^FO20,140\n");
                        jTextArea6.append("^FDREF:^FS\n");
                        jTextArea6.append("^FB720,1,,C\n");
                        jTextArea6.append("^A0N,128,86\n");
                        jTextArea6.append("^FO40,100\n");
                        jTextArea6.append("^FD" + jTextField_OFarti.getText()+"^FS\n");
                        jTextArea6.append("^FB720,2,,C\n");
                        jTextArea6.append("^FO40,220\n");
                        jTextArea6.append("^A0N,64,46\n");
                        jTextArea6.append("^FD" + jTextField_OFdescrip.getText()+ "^FS\n");
                        jTextArea6.append("^FO20,300\n");
                        jTextArea6.append("^A0N,36,24\n");
                        jTextArea6.append("^FDBULTO Nº:" + bulto + "^FS\n");
                        jTextArea6.append("^FO20,340\n");
                        jTextArea6.append("^A0N,36,24\n");
                        jTextArea6.append("^FDOC:^FS\n");
                        jTextArea6.append("^FO420,300\n");
                        jTextArea6.append("^A0N,36,24\n");
                        if (cantxbulto == bulto){
                            //if(jFormattedTextField_Pico != null && !jFormattedTextField_Pico.getText().trim().isEmpty() ){
                                if (jRadioButton_Pallet.isSelected()){
                                    //si hay picos se imprimirá la última etiqueta con el pico resultante
                                    if(Integer.parseInt(jFormattedTextField_Pico.getText())!= 0){
                                        System.out.println("pico pallet: " + jFormattedTextField_Pico.getText());
                                        jTextArea6.append("^FDCANTIDAD:" + jFormattedTextField_Pico.getText() + "^FS\n");
                                        OF = jTextField_OF.getText() + "Q" + padLeft(jFormattedTextField_Pico.getText(),4).replace(" ","0") + "E" + padLeft(Integer.toString(bulto),4).replace(" ","0");
                                        System.out.println("OF: " + OF);
                                    }else{
                                       jTextArea6.append("^FDCANTIDAD:" + p_unidades + "^FS\n"); 
                                       OF = jTextField_OF.getText() + "Q" + padLeft(Integer.toString(p_unidades),4).replace(" ","0") + "E" + padLeft(Integer.toString(bulto),4).replace(" ","0");
                                       System.out.println("OF: " + OF);
                                    }
                                       
                                }else if (jRadioButton_Caja.isSelected()){
                                    if (Integer.parseInt(jFormattedTextField_PicoCaja.getText())!=0){
                                        System.out.println("pico caja: " + jFormattedTextField_PicoCaja.getText()+ " " + cantxbulto + " " + bulto);
                                        jTextArea6.append("^FDCANTIDAD:" + jFormattedTextField_PicoCaja.getText() + "^FS\n");
                                        OF = jTextField_OF.getText() + "Q" + padLeft(jFormattedTextField_Pico.getText(),4).replace(" ","0") + "E" + padLeft(Integer.toString(bulto),4).replace(" ","0");
                                        System.out.println("OF: " + OF);
                                    }else{
                                        jTextArea6.append("^FDCANTIDAD:" + p_unidades + "^FS\n");
                                        OF = jTextField_OF.getText() + "Q" + padLeft(Integer.toString(p_unidades),4).replace(" ","0") + "E" + padLeft(Integer.toString(bulto),4).replace(" ","0");
                                    }
                                }
                        }else{
                            jTextArea6.append("^FDCANTIDAD:" + p_unidades + "^FS\n");
                            OF = jTextField_OF.getText() + "Q" + padLeft(Integer.toString(p_unidades),4).replace(" ","0") + "E" + padLeft(Integer.toString(bulto),4).replace(" ","0");
                        }
                            
                        jTextArea6.append("^BY3,2.5,80\n");
                        jTextArea6.append("^FO75,400\n");
                        jTextArea6.append("^FB750,1,,C,\n");
                        jTextArea6.append("^BCN,,Y,N,N,A\n");
                        jTextArea6.append("^FD" + referencia + "LOT" + numLote + "^FS\n");
                        jTextArea6.append("^BY2,2.5,80\n");
                        jTextArea6.append("^FO75,575\n");
                        jTextArea6.append("^FB750,1,,C,\n");
                        jTextArea6.append("^BCN,,Y,N,N,A\n");
                        //jTextArea6.append("^FD" + jTextField_OF.getText() + "^FS\n");
                        jTextArea6.append("^FD" + OF + "^FS\n");
                        jTextArea6.append("^FO20,750\n");
                        jTextArea6.append("^A0N,36,24\n");
                        jTextArea6.append("^FDMAQ:^FS\n");
                        jTextArea6.append("^XZ\n");
                        }
              }
            }
            else if (p_modo == 7){
                //aquí etiqueta UBICACIONES
                jTextArea7.append("^XA\n");
                jTextArea7.append("^CI28\n"); //juego de caracterse UTF8
                jTextArea7.append("^PW832\n");
                jTextArea7.append("^FO20,20^XGE:LD5.JPG,1,1^FS\n");
                jTextArea7.append("^PON\n");
                //jTextArea5.append("^LH0,0\n");
                //jTextArea4.append("^FO20,150\n");
                //jTextArea4.append("^GB760,600,4^FS\n"); //RECUADRO
                jTextArea7.append("^FO50,350\n");
                jTextArea7.append("^FB720,1,,C\n");
                jTextArea7.append("^A0N," + p_unidades + ",\n");
                jTextArea7.append("^FD" + p_codigo + "^FS\n");
                jTextArea7.append("^BY4,3,140\n");
                jTextArea7.append("^FO30,500\n");
                jTextArea7.append("^FB720,1,,C,\n");
                jTextArea7.append("^BCN,,N,N,N,A\n");
                jTextArea7.append("^FD" + p_codigo + "^FS\n");
                jTextArea7.append("^XZ\n");
            }
            else if (p_modo == 8){
                //aquí etiqueta Simple de Referencia
                int contador;
                for (contador = 0; contador < p_num; contador++) {
                    jTextArea8.append("^XA\n");
                    jTextArea8.append("^PON\n");
                    jTextArea8.append("^CI28\n"); //juego de caracterse UTF8
                    jTextArea8.append("^PW832\n");//Ancho de página ajustado a 10cm
                    jTextArea8.append("^FO20,20^XGE:LD5.JPG,1,1^FS\n");
                    //jTextArea2.append("^LH0,0\n");
                    jTextArea8.append("^FO50,150\n");
                    jTextArea8.append("^FO370,50\n");
                    jTextArea8.append("^BY1,,10\n");
                    jTextArea8.append("^BQN,2,4\n");
                    jTextArea8.append("^FDMM,www.denox.eu^FS\n");
                    jTextArea8.append("^FB720,2,,C\n");
                    jTextArea8.append("^A0,58,38\n");
                    jTextArea8.append("^FD www.denox.eu ^FS\n");
                    jTextArea8.append("^FO50,250\n");
                    jTextArea8.append("^FB720,2,,C\n");
                    jTextArea8.append("^A0,64,48\n");
                    jTextArea8.append("^FD " + jText_EtqSimple_descrip.getText() + "^FS\n");
                    jTextArea8.append("^FO50,450\n");
                    jTextArea8.append("^FB720,1,,C\n");
                    jTextArea8.append("^A0,64,48\n");
                    jTextArea8.append("^FD REF: " + jText_EtqSimple_ref.getText() + "^FS\n");
                    jTextArea8.append("^FO275,550\n");
                    jTextArea8.append("^FB720,1,,C\n");
                    jTextArea8.append("^BY3,2.5,200\n");
                    jTextArea8.append("^BEN,100,Y,N\n");
                    jTextArea8.append("^FD" + jText_EtqSimple_Ean.getText() + "^FS\n");
                    jTextArea8.append("^XZ\n");
                }
            }
            else if (p_modo == 9){
                //aquí etiqueta para ElCorte
                String separador = Character.toString((char)92); 
                int contador;
                for (contador = 0; contador < p_num; contador++) {
                    System.out.println("Pinto linea: contador: "+ contador + " Pnum: " + p_num);
                    jTextArea9.append("^XA\n");
                    jTextArea9.append("^PON\n");
                    jTextArea9.append("^CI28\n");
                    jTextArea9.append("^PW832\n");
                    jTextArea9.append("^FO20,20^XGE:LD5.JPG,1,1^FS\n");
                    jTextArea9.append("^FO40,100\n");
                    jTextArea9.append("^GB170,100,5^FS\n");
                    jTextArea9.append("^FO220,100\n");
                    jTextArea9.append("^GB170,100,5^FS\n");
                    jTextArea9.append("^FO400,100\n");
                    jTextArea9.append("^GB170,100,5^FS\n");
                    jTextArea9.append("^FO580,100\n");
                    jTextArea9.append("^GB170,100,5^FS\n");
                    
                    jTextArea9.append("^FO50, 120\n");
                    jTextArea9.append("^FB160,3,,C\n");
                    jTextArea9.append("^A0,24,28\n");
                    //jTextArea9.append("^FDSUCURSAL \&------\& 0588^FS\n"); // La contrabarra no le gusta a java...
                    jTextArea9.append("^FDSUCURSAL" + separador + "&------" + separador +"& " + jTextField_ElCorte_Sucursal.getText()+ "^FS\n");
                    jTextArea9.append("^FO230, 120\n");
                    jTextArea9.append("^FB160,3,,C\n");
                    jTextArea9.append("^A0,24,28\n");
                    //jTextArea9.append("^FDDEPART \&------\& 369^FS\n");
                    jTextArea9.append("^FDDEPART " + separador + "&------" + separador +"& " + jTextField_ElCorte_Depart.getText()+ "^FS\n");
                    
                    jTextArea9.append("^FO410, 120\n");
                    jTextArea9.append("^FB160,3,,C\n");
                    jTextArea9.append("^A0,24,28\n");
                    //jTextArea9.append("^FDN.PEDIDO \&------\& 33116188^FS\n");
                    jTextArea9.append("^FDN.PEDIDO " + separador + "&------" + separador + "& " + jTextField_ElCorte_Ped.getText() + "^FS\n");
                    
                    jTextArea9.append("^FO590, 120\n");
                    jTextArea9.append("^FB160,3,,C\n");
                    jTextArea9.append("^A0,24,28\n");
                    //jTextArea9.append("^FDN.ALBARAN \&------\& 2108071^FS\n");
                    jTextArea9.append("^FDN.ALBARAN " + separador + "&------" + separador + "& " + jTextField_ElCorte_Alb2.getText() + "^FS\n");
                    
                    jTextArea9.append("^FO040,260\n");
                    jTextArea9.append("^GB520,100,5^FS\n");
                    jTextArea9.append("^FO580,260\n");
                    jTextArea9.append("^GB170,100,5^FS\n");
                    jTextArea9.append("^A0,24,28\n");
                    jTextArea9.append("^FB520,1,,C\n");
                    jTextArea9.append("^FO50, 270\n");
                    jTextArea9.append("^FDARTICULO^FS\n");
                    
                    jTextArea9.append("^FO50, 305\n");
                    jTextArea9.append("^FB520,2,,L\n");
                    jTextArea9.append("^A0,24,28\n");
                    jTextArea9.append("^FD"+ jText_ElCorte_RefCte.getText() + " " +  jText_ElCorte_Descrip.getText()+"^FS\n");
                    
                    jTextArea9.append("^FO410,270\n");
                    jTextArea9.append("^FB520,3,,C\n");
                    jTextArea9.append("^A0,24,28\n");
                    //jTextArea9.append("^FDCOLOR \&------ \& GRIS/VERDE^FS\n");
                    jTextArea9.append("^FDCOLOR " + separador + "&------ " + separador + "& "+ jTextField_ElCorte_Color.getText() + "^FS\n");
                    
                    jTextArea9.append("^FO40,420\n");
                    jTextArea9.append("^GB220,120,5^FS\n");
                    jTextArea9.append("^FO280,420\n");
                    jTextArea9.append("^GB220,120,5^FS\n");
                    jTextArea9.append("^FO520,420\n");
                    jTextArea9.append("^GB230,120,5^FS\n");
                    
                    jTextArea9.append("^FO50,430\n");
                    jTextArea9.append("^FB210,4,,C\n");
                    jTextArea9.append("^A0,24,28\n");
                    //jTextArea9.append("^FD REFERENCIA \& CORTE INGLES\&------ \&63100122^FS\n");
                    jTextArea9.append("^FD REFERENCIA " + separador + "& CORTE INGLES" + separador + "&------ " + separador+ "&" + jText_ElCorte_RefCte.getText() + "^FS\n");
                    
                    jTextArea9.append("^FO290,430\n");
                    jTextArea9.append("^FB210,4,,C\n");
                    jTextArea9.append("^A0,24,28\n");
                    //jTextArea9.append("^FD NUMERO DE \& BULTOS \&------\& 1/1^FS\n");
                    jTextArea9.append("^FD NUMERO DE " + separador + "& BULTOS " + separador + "&------" + separador + "& " + (contador +1) + "/" + p_num +"^FS\n");
                    
                    jTextArea9.append("^FO530,430\n");
                    jTextArea9.append("^FB210,4,,C\n");
                    jTextArea9.append("^A0,24,28\n");
                    //jTextArea9.append("^FD UNIDADES \& POR CAJA \&------\& 2^FS\n");
                    jTextArea9.append("^FD UNIDADES " + separador + "& POR CAJA " + separador + "&------" + separador + "& "+ jTextField_ElCorte_UdCaja.getText() + "^FS\n");
                    
                    jTextArea9.append("^FO50,580\n");
                    jTextArea9.append("^A0,28,40\n");
                    jTextArea9.append("^FD PROVEEDOR: FABRICANTES DE MENAJE^FS\n");
                    jTextArea9.append("^XZ\n");
                }
            }
            
            else if (p_modo == 10){
                //aquí etiquetas SSCC
                 int contador;
                 int bultos = 0;
                 System.out.println(" En zpl: " + rs.getString("COD_SSCC"));
                 
                for (contador = 0; contador < p_num; contador++) {
                    //String cod_ean = rs.getString("EAN");
                    //String cod_ean = jText_ref_ean.getText();
                    bultos = bultos + 1;
                    jTextArea12.append("ªXA\n");
                    jTextArea12.append("ªPQ2\n");
                    //jTextArea12.append("ªPON\n"); en las impresoras de XPO genera un caracter extraño, por eso lo quito.
                    jTextArea12.append("ªLH120,50\n");
                    jTextArea12.append("ªA0,48,34\n");
                    jTextArea12.append("ªFB800,1,,L\n");
                    jTextArea12.append("ªFDFABRICANTES DE MENAJE, S.A (FAMESA)ªFS\n");
                    jTextArea12.append("ªFO00,40\n");
                    jTextArea12.append("ªFB800,2,,L\n");
                    jTextArea12.append("ªA0,24,20\n");
                    jTextArea12.append("ªFDCtra. Nacional 330 Km. 486 Pol. Ind. AGRINASA, Nave 30-35\\& 50420 CADRETE (Zaragoza)ªFS\n");
                    jTextArea12.append("ªFO00,100\n");
                    jTextArea12.append("ªFB800,2,,L\n");
                    jTextArea12.append("ªA0,24,20\n");
                    jTextArea12.append("ªFDARTICULOªFS\n");
                    jTextArea12.append("ªFO00,95\n");
                    jTextArea12.append("ªGB600,0,2ªFS\n");
                    jTextArea12.append("ªFO00,120\n");
                    jTextArea12.append("ªFB800,2,,L\n");
                    jTextArea12.append("ªA0,48,34\n");
                    jTextArea12.append("ªFD" + rs.getString("ITM_DESC") + "ªFS\n");
                    jTextArea12.append("ªFO00,160\n");
                    jTextArea12.append("ªGB600,0,2ªFS\n");
                    jTextArea12.append("ªFO00,180\n");
                    jTextArea12.append("ªFB800,2,,L\n");
                    jTextArea12.append("ªA0,24,20\n");
                    jTextArea12.append("ªFDGTIN:   " + rs.getString("GTIN") + "\\&CAJAS:" + rs.getString("CAJAS") + "ªFS\n");  
                    jTextArea12.append("ªFO00,240\n");
                    jTextArea12.append("ªFB800,1,,L\n");
                    jTextArea12.append("ªA0,48,34\n");
                    jTextArea12.append("ªFDLOTE: " + rs.getString("LOTE") + "ªFS\n");
                    jTextArea12.append("ªFO00,280\n");
                    jTextArea12.append("ªGB600,0,2ªFS\n");
                    jTextArea12.append("ªFO00,320\n");
                    jTextArea12.append("ªFB800,1,,L\n");
                    jTextArea12.append("ªA0,56,40\n");
                    jTextArea12.append("ªFDSSCC:       " + rs.getString("COD_SSCC") + "ªFS\n");
                    jTextArea12.append("ªFO00,360\n");
                    jTextArea12.append("ªGB600,0,2ªFS\n");
                    jTextArea12.append("ªBY2,2.5,260\n");
                    jTextArea12.append("ªFO20,525\n");
                    jTextArea12.append("ªBCN,,Y,N,N,D\n");
                    //jTextArea12.append("ªFD(01)4841302010282>8(10)F230101ªFS\n");
                    //jTextArea12.append("ªFD(01)" + rs.getString("GTIN") + ">8(10)" + rs.getString("LOTE") + "ªFS\n");
                    //System.out.println("tamaño gtin: " + rs.getString("GTIN").length());
                                       
                    try{ 
                        jTextArea12.append("ªFD(01)" + rs.getString("GTIN").substring(0,13) + ">8(10)" + rs.getString("LOTE") + "ªFS\n");
                    } catch (IndexOutOfBoundsException err){ 
                    JOptionPane.showMessageDialog(this, err.getMessage() + " -No hay EAN14 DEFINIDO para el artículo " + rs.getString("ITMREF_0"));
                        
                    }
                    
                    jTextArea12.append("ªBY2,2.5,260\n");
                    jTextArea12.append("ªFO20,835\n");
                    jTextArea12.append("ªBCN,,Y,N,N,D\n");
                    //jTextArea12.append("ªFD(00)384130200000019571ªFS\n");
                    jTextArea12.append("ªFD(00)" + rs.getString("COD_SSCC") + "ªFS\n");
                    jTextArea12.append("ªXZ\n");
                  
                }
            }
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
    
        

    }

    public static String padRight(String s, int n) { //función para añadir n espacios hacia la derecha
     return String.format("%-" + n + "s", s);  
    }

    public static String padLeft(String s, int n) { //función para añadir n espacios hacia la izquierda
        return String.format("%" + n + "s", s);  
    }
  
    
    public void ejecutar_sendftp(String cmd){
    Process p;
    try {
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = "";
      while ((line = reader.readLine())!= null) {
          if(jDialog10.isVisible())
              jTextArea_xpo.append(line + "\n");
          
          System.out.println(line);
      }
      /*int exitVal = process.waitFor();
		if (exitVal == 0) {
			System.out.println("Success!");
			System.out.println(output);
			System.exit(0);
		} else {
			//abnormal...
		}*/
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
    
  public void ejecutarCMD(String cmd){
    Process p;
    try {
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = "";
      while ((line = reader.readLine())!= null) {
          if(jDialog5.isVisible())
              jTextArea_NetUse.append(line + "\n");
          
          System.out.println(line);
      }
      /*int exitVal = process.waitFor();
		if (exitVal == 0) {
			System.out.println("Success!");
			System.out.println(output);
			System.exit(0);
		} else {
			//abnormal...
		}*/
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }    
    
    public void check_radiobuttons_OF(){
        //pinto y despinto los textfields en base a la selección de los radiobutton (es una ayuda visual exclusivamente)
        if (jRadioButton_OF.isSelected() ){ //OF
            jFormattedTextField_CantPadrePallet.setEnabled(false);
            jFormattedTextField_CantPadreCaja.setEnabled(false);
            
            if (jRadioButton_Pallet.isSelected()){//OF + PALLET
                jFormattedTextField_CantPallet.setEnabled(true);
                jFormattedTextField_CantCaja.setEnabled(false);
                jFormattedTextField_CantOF.setEnabled(false);
            }else if(jRadioButton_Caja.isSelected()){//OF + CAJAS
                jFormattedTextField_CantCaja.setEnabled(true);
                jFormattedTextField_CantOF.setEnabled(false);
                jFormattedTextField_CantPallet.setEnabled(false);
                
            }else{//OF + PRODUCTO
                jFormattedTextField_CantOF.setEnabled(true);
                jFormattedTextField_CantPallet.setEnabled(false);
                jFormattedTextField_CantCaja.setEnabled(false);
            }
        
                
        }else{//PADRE
            jFormattedTextField_CantCaja.setEnabled(false);
            jFormattedTextField_CantPallet.setEnabled(false);
                        
            if (jRadioButton_Pallet.isSelected()){//PADRE + PALLET
                jFormattedTextField_CantPadrePallet.setEnabled(true);
                jFormattedTextField_CantPadreCaja.setEnabled(false);
                jFormattedTextField_CantOF.setEnabled(false);
            }else if(jRadioButton_Caja.isSelected()){//PADRE + CAJAS
                jFormattedTextField_CantPadreCaja.setEnabled(true);
                jFormattedTextField_CantOF.setEnabled(false);
                jFormattedTextField_CantPadrePallet.setEnabled(false);
                
            }else{//PADRE + PRODUCTO
                jFormattedTextField_CantOF.setEnabled(true);
                jFormattedTextField_CantPadrePallet.setEnabled(false);
                jFormattedTextField_CantPadreCaja.setEnabled(false);
            }
            
        }
    }
    
    public void check_radiobutton_tipo(){
        //habilito o deshabilito los radiobutton de las impresoras según el tipo de etiqueta
        if (jRadioButton_Caja.isSelected() || jRadioButton_Pallet.isSelected() || jRadioButton_Producto.isSelected() || jRadioButton_Kety.isSelected()) {
            if (jRadioButton_Caja.isSelected() || jRadioButton_Pallet.isSelected()){
                System.out.println("Radio de caja y pallet cambiada");
                jRadioButton_Zebra1.setSelected(true);
                
                jRadioButton_Zebra1.setEnabled(true);
                jRadioButton_Zebra2.setEnabled(true);
                jRadioButton_Godex1.setEnabled(false);
                jRadioButton_Godex2.setEnabled(false);
            }else{
                System.out.println("radio producto cambiada");
                jRadioButton_Godex1.setSelected(true);
                
                jRadioButton_Zebra1.setEnabled(false);
                jRadioButton_Zebra2.setEnabled(false);
                jRadioButton_Godex1.setEnabled(true);
                jRadioButton_Godex2.setEnabled(true);
            }
            
        }
    }
    
    public void check_radiobutton_ubi(){
          //AL CAMBIAR DE RADIOBUTTON RELLENO EL COMBO CORRESPONDIENTE A LA PLANTA SELECCIONADA
        jComboBox_Ubi.removeAllItems();
         if (jRadioButton_UbiFam.isSelected()){
            System.out.println("ubicaciones famesa...");
                             
         try {

             String query = "SELECT DISTINCT LOCTYP_0 AS TIPO_UBI\n" +
                            "FROM LIVE.STOLOC\n" +
                            "WHERE STOFCY_0 = 'FAM'";
             
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
         
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_Ubi.addItem(rs.getString("TIPO_UBI"));
            }
        } catch (SQLException err) {
                JOptionPane.showMessageDialog(this, err.getMessage());
        }
        }else{
            System.out.println("ubicaciones Trilla...");
        try {

              String query = "SELECT DISTINCT LOCTYP_0 AS TIPO_UBI\n" +
                            "FROM LIVE.STOLOC\n" +
                            "WHERE STOFCY_0 = 'TRI'";
             
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();

            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                 jComboBox_Ubi.addItem(rs.getString("TIPO_UBI"));
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        }
        }
    }
    public void consultar_tipoUbi(){
        //busco las ubicaciones entre el mínimo y el máximo 
        System.out.println("consultar MIN/MAX ubicaciones");
        try {

            String query = "SELECT MIN (LOC_0) AS LOC_INI, MAX(LOC_0) AS LOC_FIN\n"
                      + "FROM LIVE.STOLOC\n"
                      + "WHERE LOCTYP_0 = '" + jComboBox_Ubi.getSelectedItem().toString() + "'";
             
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
          
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                jText_UbiDesde.setText(rs.getString("LOC_INI"));
                jText_UbiHasta.setText(rs.getString("LOC_FIN"));
                
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        } 
        
    }
    public int contar_Ubicaciones(String pUbiDesde, String pUbiHasta) {
        //busco las ubicaciones entre el mínimo y el máximo 
        System.out.println("consultar MIN/MAX ubicaciones");
        try {

              String query = "SELECT COUNT(*) AS NUM_UBI\n"
                      + "FROM LIVE.STOLOC\n"
                      + "WHERE LOCTYP_0 = '" + jComboBox_Ubi.getSelectedItem().toString() + "'\n"
                      + "AND LOC_0 BETWEEN '" + pUbiDesde + "' and '" + pUbiHasta + "'";
              
             
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
          
            while (rs.next()) { //muevo el cursor al primer registro y relleno el combobox
                jFormattedTextField_cantidadUbi.setText(rs.getString("NUM_UBI"));
                Integer.parseInt(jFormattedTextField_cantidadUbi.getText());
                            
                
            }

        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());

        } 
        return 0;
    }
    
    
	
    
  /*  public void Imagen() {
this.setSize(300, 400); //se selecciona el tamaño del panel
}
 
//Se crea un método cuyo parámetro debe ser un objeto Graphics
 
public void paint(Graphics grafico) {
Dimension height = getSize();
 
//Se selecciona la imagen que tenemos en el paquete de la //ruta del programa
 
ImageIcon Img = new ImageIcon(getClass().getResource("/Images/Diagrama.png")); 
 
//se dibuja la imagen que tenemos en el paquete Images //dentro de un panel
 
grafico.drawImage(Img.getImage(), 0, 0, height.width, height.height, null);
 
setOpaque(false);
super.paintComponent(grafico);
}*/
    
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
            java.util.logging.Logger.getLogger(Entregas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Entregas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Entregas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Entregas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Entregas().setVisible(true);
            }
        });
        
        
 
       
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup_DatosAImp;
    private javax.swing.ButtonGroup buttonGroup_Impresoras;
    private javax.swing.ButtonGroup buttonGroup_ReferenciaSimple;
    private javax.swing.ButtonGroup buttonGroup_SSCC;
    private javax.swing.ButtonGroup buttonGroup_TipoEti;
    private javax.swing.ButtonGroup buttonGroup_Ubicaciones;
    private javax.swing.ButtonGroup buttonGroup_alb;
    private javax.swing.ButtonGroup buttonGroup_idioma;
    private javax.swing.ButtonGroup buttonGroup_idioma_alb;
    private javax.swing.ButtonGroup buttonGroup_stocks;
    private javax.swing.ButtonGroup buttonGroup_tipo_transportista;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButton_ConsultaAlbNormal;
    private javax.swing.JButton jButton_ConsultaAlbNormal1;
    private javax.swing.JButton jButton_ConsultaOF;
    private javax.swing.JButton jButton_ElCorte_Generar;
    private javax.swing.JButton jButton_ElCorte_Print;
    private javax.swing.JButton jButton_EtqSimple_Generar;
    private javax.swing.JButton jButton_EtqSimple_Imprimir;
    private javax.swing.JButton jButton_ItmLot;
    private javax.swing.JButton jButton_RefrescarAlb;
    private javax.swing.JButton jButton_RefrescarAlb1;
    private javax.swing.JButton jButton_RefrescarAlb2;
    private javax.swing.JButton jButton_UbiConsulta;
    private javax.swing.JButton jButton_consultar;
    private javax.swing.JButton jButton_consultar_registro;
    private javax.swing.JComboBox jComboBox_AlbNormal;
    private javax.swing.JComboBox jComboBox_AlbSSCC;
    private javax.swing.JComboBox jComboBox_OF;
    private javax.swing.JComboBox jComboBox_Padres;
    private javax.swing.JComboBox jComboBox_Stock;
    private javax.swing.JComboBox jComboBox_Ubi;
    private javax.swing.JComboBox jCombo_ElCorte;
    private javax.swing.JComboBox jCombo_EtqSimple_ref;
    private javax.swing.JComboBox jCombo_pedpro;
    private javax.swing.JComboBox jCombo_ref;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JDialog jDialog10;
    private javax.swing.JDialog jDialog11;
    private javax.swing.JDialog jDialog2;
    private javax.swing.JDialog jDialog3;
    private javax.swing.JDialog jDialog4;
    private javax.swing.JDialog jDialog5;
    private javax.swing.JDialog jDialog6;
    private javax.swing.JDialog jDialog7;
    private javax.swing.JDialog jDialog8;
    private javax.swing.JDialog jDialog9;
    private javax.swing.JFormattedTextField jFormattedTextField_CantCaja;
    private javax.swing.JFormattedTextField jFormattedTextField_CantElCorte;
    private javax.swing.JFormattedTextField jFormattedTextField_CantOF;
    private javax.swing.JFormattedTextField jFormattedTextField_CantPadreCaja;
    private javax.swing.JFormattedTextField jFormattedTextField_CantPadrePallet;
    private javax.swing.JFormattedTextField jFormattedTextField_CantPallet;
    private javax.swing.JFormattedTextField jFormattedTextField_CantRef;
    private javax.swing.JFormattedTextField jFormattedTextField_CapCaja;
    private javax.swing.JFormattedTextField jFormattedTextField_CapPallet;
    private javax.swing.JFormattedTextField jFormattedTextField_Pico;
    private javax.swing.JFormattedTextField jFormattedTextField_PicoCaja;
    private javax.swing.JFormattedTextField jFormattedTextField_cantidad;
    private javax.swing.JFormattedTextField jFormattedTextField_cantidad1;
    private javax.swing.JFormattedTextField jFormattedTextField_cantidadUbi;
    private javax.swing.JFormattedTextField jFormattedTextField_cantidadUbi1;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize1;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize2;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize3;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_albaran;
    private javax.swing.JLabel jLabel_logo2;
    private javax.swing.JLabel jLabel_logo3;
    private javax.swing.JLabel jLabel_logo4;
    private javax.swing.JLabel jLabel_logo5;
    private javax.swing.JLabel jLabel_logo6;
    private javax.swing.JLabel jLabel_logo7;
    private javax.swing.JLabel jLabel_logo8;
    private javax.swing.JLabel jLabel_logo9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane10;
    private javax.swing.JLayeredPane jLayeredPane11;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JLayeredPane jLayeredPane3;
    private javax.swing.JLayeredPane jLayeredPane4;
    private javax.swing.JLayeredPane jLayeredPane5;
    private javax.swing.JLayeredPane jLayeredPane6;
    private javax.swing.JLayeredPane jLayeredPane7;
    private javax.swing.JLayeredPane jLayeredPane8;
    private javax.swing.JLayeredPane jLayeredPane9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem_OF;
    private javax.swing.JMenuItem jMenuItem_SSCC;
    private javax.swing.JMenuItem jMenuItem_alb_dascher;
    private javax.swing.JMenuItem jMenuItem_albaranNormal;
    private javax.swing.JMenuItem jMenuItem_albaranes;
    private javax.swing.JMenuItem jMenuItem_referencias;
    private javax.swing.JMenuItem jMenuItem_salir;
    private javax.swing.JMenuItem jMenuItem_tonta;
    private javax.swing.JMenu jMenu_ir;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel52;
    private javax.swing.JPanel jPanel53;
    private javax.swing.JPanel jPanel54;
    private javax.swing.JPanel jPanel55;
    private javax.swing.JPanel jPanel56;
    private javax.swing.JPanel jPanel57;
    private javax.swing.JPanel jPanel58;
    private javax.swing.JPanel jPanel59;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel60;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton_Caja;
    private javax.swing.JRadioButton jRadioButton_DASCHER;
    private javax.swing.JRadioButton jRadioButton_Godex1;
    private javax.swing.JRadioButton jRadioButton_Godex2;
    private javax.swing.JRadioButton jRadioButton_Kety;
    private javax.swing.JRadioButton jRadioButton_Logo;
    private javax.swing.JRadioButton jRadioButton_MRW;
    private javax.swing.JRadioButton jRadioButton_OF;
    private javax.swing.JRadioButton jRadioButton_Padre;
    private javax.swing.JRadioButton jRadioButton_Pallet;
    private javax.swing.JRadioButton jRadioButton_PlantaF;
    private javax.swing.JRadioButton jRadioButton_PlantaT;
    private javax.swing.JRadioButton jRadioButton_Producto;
    private javax.swing.JRadioButton jRadioButton_UbiFam;
    private javax.swing.JRadioButton jRadioButton_UbiTri;
    private javax.swing.JRadioButton jRadioButton_XPO;
    private javax.swing.JRadioButton jRadioButton_Zebra1;
    private javax.swing.JRadioButton jRadioButton_Zebra2;
    private javax.swing.JRadioButton jRadioButton_alb_fam;
    private javax.swing.JRadioButton jRadioButton_alb_fam1;
    private javax.swing.JRadioButton jRadioButton_alb_tri;
    private javax.swing.JRadioButton jRadioButton_alb_tri1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTable jTb;
    private javax.swing.JTable jTb_dascher;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea10;
    private javax.swing.JTextArea jTextArea11;
    private javax.swing.JTextArea jTextArea12;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextArea jTextArea5;
    private javax.swing.JTextArea jTextArea6;
    private javax.swing.JTextArea jTextArea7;
    private javax.swing.JTextArea jTextArea8;
    private javax.swing.JTextArea jTextArea9;
    private javax.swing.JTextArea jTextArea_NetUse;
    private javax.swing.JTextArea jTextArea_log;
    private javax.swing.JTextArea jTextArea_xpo;
    private javax.swing.JTextField jTextField_AlbBultos;
    private javax.swing.JTextField jTextField_AlbBultos1;
    private javax.swing.JTextField jTextField_AlbCte;
    private javax.swing.JTextField jTextField_AlbCte1;
    private javax.swing.JTextField jTextField_AlbDir2;
    private javax.swing.JTextField jTextField_AlbDir3;
    private javax.swing.JTextField jTextField_AlbDirEnv;
    private javax.swing.JTextField jTextField_AlbDirEnv1;
    private javax.swing.JTextField jTextField_AlbDirPostal;
    private javax.swing.JTextField jTextField_AlbDirPostal1;
    private javax.swing.JTextField jTextField_AlbNomCte;
    private javax.swing.JTextField jTextField_AlbNomCte1;
    private javax.swing.JTextField jTextField_AlbNumAlb;
    private javax.swing.JTextField jTextField_AlbNumAlb1;
    private javax.swing.JTextField jTextField_AlbPedidoCte;
    private javax.swing.JTextField jTextField_AlbPedidoCte1;
    private javax.swing.JTextField jTextField_AlbPrep;
    private javax.swing.JTextField jTextField_AlbPrep1;
    private javax.swing.JTextField jTextField_AlbTrans;
    private javax.swing.JTextField jTextField_AlbTrans1;
    private javax.swing.JTextField jTextField_ElCorte_Alb2;
    private javax.swing.JTextField jTextField_ElCorte_Art;
    private javax.swing.JTextField jTextField_ElCorte_Color;
    private javax.swing.JTextField jTextField_ElCorte_Cte;
    private javax.swing.JTextField jTextField_ElCorte_Depart;
    private javax.swing.JTextField jTextField_ElCorte_Ped;
    private javax.swing.JTextField jTextField_ElCorte_Sucursal;
    private javax.swing.JTextField jTextField_ElCorte_UdCaja;
    private javax.swing.JTextField jTextField_EtqSimple_lan;
    private javax.swing.JTextField jTextField_OF;
    private javax.swing.JTextField jTextField_OFarti;
    private javax.swing.JTextField jTextField_OFdescrip;
    private javax.swing.JTextField jTextField_OfEan;
    private javax.swing.JTextField jTextField_OfKit;
    private javax.swing.JTextField jTextField_OfLote;
    private javax.swing.JTextField jTextField_PadreArti;
    private javax.swing.JTextField jTextField_PadreDesc;
    private javax.swing.JTextField jTextField_PadreEan;
    private javax.swing.JTextField jTextField_SSCC_pallets;
    private javax.swing.JTextField jTextStock_arti;
    private javax.swing.JTextField jTextStock_lote;
    private javax.swing.JTextField jText_ElCorte_Alb;
    private javax.swing.JTextField jText_ElCorte_Descrip;
    private javax.swing.JTextField jText_ElCorte_RefCte;
    private javax.swing.JTextField jText_EtqSimple_Ean;
    private javax.swing.JTextField jText_EtqSimple_descrip;
    private javax.swing.JTextField jText_EtqSimple_ref;
    private javax.swing.JTextField jText_StockDesde;
    private javax.swing.JTextField jText_StockHasta;
    private javax.swing.JTextField jText_UbiDesde;
    private javax.swing.JTextField jText_UbiHasta;
    private javax.swing.JTextField jText_alb_dascher;
    private javax.swing.JTextField jText_albaran;
    private javax.swing.JTextField jText_empresa;
    private javax.swing.JTextField jText_empresa1;
    private javax.swing.JTextField jText_pedproNumped;
    private javax.swing.JTextField jText_pedproProv;
    private javax.swing.JTextField jText_ref_capacidad;
    private javax.swing.JTextField jText_ref_cliente;
    private javax.swing.JTextField jText_ref_desc_art;
    private javax.swing.JTextField jText_ref_desc_cte;
    private javax.swing.JTextField jText_ref_ean;
    private javax.swing.JTextField jText_referencia;
    private javax.swing.JTextField jTonta_texto;
    private javax.swing.JTextField jTonta_texto1;
    private javax.swing.JTextField jTonta_texto2;
    private javax.swing.JTextField jTonta_texto3;
    private javax.swing.JTextField jTonta_texto4;
    // End of variables declaration//GEN-END:variables
}
