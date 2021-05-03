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


//import para jtable
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;



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
            String host = "jdbc:sqlserver://192.168.50.10:49412;databaseName=x3famesa;schema=FAMESAOF";
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
            String host = "jdbc:sqlserver://192.168.28.100:51439;databaseName=x3;schema=TEST";
            String uName = "it";
            String uPass = "@System345";
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
        jMenuItem2 = new javax.swing.JMenuItem();
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
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
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
                                    .addComponent(jCombo_ref, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addComponent(jLabel_logo2, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
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
                        .addComponent(jComboBox_AlbNormal, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_AlbNomCte, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
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
                        .addGap(48, 48, 48)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jLayeredPane3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
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
                .addContainerGap(84, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLayeredPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
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

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jRadioButton_Pallet, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton_Producto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton_Caja, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addComponent(jRadioButton_Producto)
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
        jFormattedTextField_fontSize3.setText("164");
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Generación de etiquetas");
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
        jMenu_ir.add(jSeparator2);

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
    
    private void consultar_albaran_normal() {
        //LLenamos el ComboBox de albaranes
        
        jTextArea4.setText(null);
        try {

            /*String query = "SELECT DEL.SOHNUM_0 AS NUMPED, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0 AS TRANSPORTISTA, DEL.BPCORD_0,\n"
                    + " DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0 as DESTINO, ORD.CUSORDREF_0 AS NUMPED, DEL.YPACK_0 AS BULTOS,\n"
                    + "ADR.BPAADDLIG_0 AS DIR1, ADR.BPAADDLIG_1 AS DIR2, ADR.POSCOD_0 AS CP, \n"
                    + "ADR.CTY_0 AS CITY, ADR.SAT_0 AS PROVINCIA, ADR.CRYNAM_0 AS PAIS\n"
                    + "fROM FAMESAOF.SDELIVERY DEL\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN FAMESAOF.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    + "WHERE DEL.SOHNUM_0='" + jTextField_AlbNumAlb.getText() + "'";*/
             String query = "SELECT DEL.SOHNUM_0 AS PEDIDO_DENOX, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0 AS TRANSPORTISTA, DEL.BPCORD_0,\n"
                    + " DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0 as DESTINO, ORD.CUSORDREF_0 AS NUMPED, DEL.YPACK_0 AS BULTOS,\n"
                    + "ADR.BPAADDLIG_0 AS DIR1, ADR.BPAADDLIG_1 AS DIR2, ADR.POSCOD_0 AS CP, \n"
                    + "ADR.CTY_0 AS CITY, ADR.SAT_0 AS PROVINCIA, ADR.CRYNAM_0 AS PAIS, VAL2.PREUSR_0 AS PREPARADOR\n"
                    + "fROM FAMESAOF.SDELIVERY DEL\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN FAMESAOF.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN FAMESAOF.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN FAMESAOF.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
                    + "WHERE DEL.SDHNUM_0='" + jTextField_AlbNumAlb.getText() + "'";
                 
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
                            +", CAST(CAB.EXTQTY_0 /  CASE (ITM.ZITMDUNCAP_0) WHEN 0 THEN 1"
                            +"                                    ELSE ISNULL(ITM.ZITMDUNCAP_0,1) END AS INTEGER) AS CANT_CAJA\n" 
                            +", (CAST(CAB.EXTQTY_0 / CASE (ITF.PCKCAP_0) WHEN 0 THEN 1\n"
                            +"                                    ELSE ISNULL(ITF.PCKCAP_0,1) END AS INTEGER) )\n"
		            +"					+ CASE(MGI.UOMEXTQTY_0 % CASE (ITF.PCKCAP_0) WHEN 0 THEN 1 ELSE ITF.PCKCAP_0 END) WHEN 0 THEN 0 ELSE 1 END\n"
			    +"                                      AS CANT_PALLET\n"
                            +", ITM.EANCOD_0 AS EANCOD, MGI.LOT_0 AS LOTE\n"
                            +", CASE(ITS.ITMTYP_0) WHEN 1 THEN 'NORMAL' WHEN 3 THEN 'KIT' ELSE 'ESTRUCTURA' END AS KIT\n"
                            +", CAST(ITF.PCKCAP_0 AS INTEGER) as CAPACIDAD_PALLET\n"
                            +", CAST(MGI.UOMEXTQTY_0 % CASE (ITF.PCKCAP_0) WHEN 0 THEN 1 ELSE ITF.PCKCAP_0 END AS INTEGER) AS PICO_PALLET\n"
                            +", CAST(ITM.ZITMDUNCAP_0 AS INTEGER) AS CAPACIDAD_CAJA\n"
                            +", CAST(CAB.EXTQTY_0 % CASE (ITM.ZITMDUNCAP_0) WHEN 0 THEN 1 ELSE ITM.ZITMDUNCAP_0 END AS INTEGER) AS PICO_CAJA\n"
                            +", BOM.ITMREF_0 AS PADRE\n"
                            +"FROM FAMESAOF.MFGHEAD CAB\n"
                            +"INNER JOIN FAMESAOF.MFGITM MGI ON MGI.MFGNUM_0 = CAB.MFGNUM_0\n"
                            +"INNER JOIN FAMESAOF.ITMMASTER ITM ON MGI.ITMREF_0 = ITM.ITMREF_0\n"
                            +"INNER JOIN FAMESAOF.ITMSALES ITS ON ITS.ITMREF_0 = ITM.ITMREF_0\n"
                            +"INNER JOIN FAMESAOF.ITMFACILIT ITF ON ITF.ITMREF_0 = ITM.ITMREF_0\n"
                            +"LEFT JOIN FAMESAOF.BOMD BOM ON BOM .CPNITMREF_0 = ITM.ITMREF_0\n"
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
                            + ",  CAST(" + jFormattedTextField_CantOF.getText() + " / CASE ITM.ZITMDUNCAP_0 WHEN 0 THEN 1\n" 
                            +"  ELSE   ITM.ZITMDUNCAP_0 END AS INTEGER) AS CANT_PADRE_CAJA \n" 
                            + ",  CAST(" + jFormattedTextField_CantOF.getText() + " / CASE ITF.PCKCAP_0 WHEN 0 THEN 1\n" 
                            +"  ELSE   ITF.PCKCAP_0 END AS INTEGER) AS CANT_PADRE_PALLET\n" 
                            +"FROM FAMESAOF.ITMMASTER ITM\n" 
                            +"INNER JOIN FAMESAOF.ITMFACILIT ITF ON ITF.ITMREF_0 = ITM.ITMREF_0\n" 
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
                     +       "FROM FAMESAOF.BOMD\n" 
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

            /*String query = "SELECT TOP 100 DEL.SOHNUM_0 AS NUMPED, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPCORD_0, DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0, \n"
                    + "ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0 \n"
                    + "fROM FAMESAOF.SDELIVERY DEL\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN FAMESAOF.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    + "ORDER BY DEL.SOHNUM_0 DESC";*/
             String query = "SELECT TOP 100 DEL.SOHNUM_0 AS PEDIDO_DENOX, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPCORD_0, DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0, \n"
                    + "ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0, VAL2.PREUSR_0 AS PREPARADOR \n"
                    + "fROM FAMESAOF.SDELIVERY DEL\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN FAMESAOF.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN FAMESAOF.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN FAMESAOF.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
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

            /*String query = "SELECT TOP 100 DEL.SOHNUM_0 AS NUMPED, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPCORD_0, DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0, \n"
                    + "ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0 \n"
                    + "fROM FAMESAOF.SDELIVERY DEL\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN FAMESAOF.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    + "ORDER BY DEL.SOHNUM_0 DESC";*/
           String query = "SELECT TOP 100 DEL.SOHNUM_0 AS PEDIDO_DENOX, DEL.BPCORD_0 AS CTE, DEL.SDHNUM_0 AS NUMALB, DEL.BPTNUM_0, TRA.BPRNAM_0, DEL.BPCORD_0, DEL.BPAADD_0, BP.BPRNAM_0, DIR.BPDNAM_0, ORD.CUSORDREF_0, DEL.YPACK_0, ADR.BPAADDLIG_0, ADR.BPAADDLIG_1, ADR.POSCOD_0, \n"
                    + "ADR.CTY_0, ADR.SAT_0, ADR.CRYNAM_0, VAL2.PREUSR_0 AS PREPARADOR \n"
                    + "fROM FAMESAOF.SDELIVERY DEL\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS BP ON DEL.BPCORD_0= BP.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPARTNER AS TRA ON DEL.BPTNUM_0 = TRA.BPRNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPDLVCUST DIR ON DIR.BPAADD_0 = DEL.BPAADD_0 AND DEL.BPCORD_0 = DIR.BPCNUM_0\n"
                    + "INNER JOIN FAMESAOF.SORDER ORD ON ORD.SOHNUM_0 = DEL.SOHNUM_0\n"
                    + "INNER JOIN FAMESAOF.BPADDRESS ADR ON ADR.BPANUM_0 = DEL.BPCORD_0 AND DEL.BPAADD_0 = ADR.BPAADD_0\n"
                    //+ "INNER JOIN FAMESAOF.STOPRED VAL1 ON VAL1.ORINUM_0 = DEL.SOHNUM_0 AND VAL1.ORISEQ_0 = 1000\n"
                    + "INNER JOIN FAMESAOF.STOPREH VAL2 ON VAL2.SDHNUM_0 = DEL.SDHNUM_0\n"
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
            String query = "EXECUTE FAMESAOF.ZLANZA_ETIENVASE @pOF = '" + jTextField_OF.getText() + "', @lan = 'SPA'";
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
                     +       "FROM FAMESAOF.MFGHEAD CAB\n" 
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
        //le paso la cantidad de etiquetas a imprimir y el tamaño de la fuente
                
        try {
           
            String query = "SELECT LOC_0 AS UBI\n"
                      + "FROM TEST.STOLOC\n"
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
        String file_name = "C:/tmp/etiqueta.txt";
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
                    + "INSERT INTO FAMESAOF.ZSDELIVERY_PRINT_LOG (CODIGO, TIPO) VALUES ('" + p_codigo + "','" + p_tipo + "')"
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
           
            String query = "SELECT CODIGO FROM  FAMESAOF.ZSDELIVERY_PRINT_LOG WHERE CODIGO = '" + p_codigo + "' AND TIPO = '" + p_tipo + "'";
                    
                                   
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
                    jTextArea6.append("^H10\r\n");
                    jTextArea6.append("^P"+ p_num + "\r\n"); //número de etiquetas a imprimir
                    jTextArea6.append("^S6\r\n");
                    jTextArea6.append("^AD\r\n");
                    jTextArea6.append("^C1\r\n");
                    jTextArea6.append("^R16\r\n");
                    jTextArea6.append("~Q+0\r\n");
                    jTextArea6.append("^O0\r\n");
                    jTextArea6.append("^D0\r\n");
                    jTextArea6.append("^E12\r\n");
                    jTextArea6.append("~R255\r\n");
                    jTextArea6.append("^L\r\n");
                    jTextArea6.append("Dy2-me-dd\r\n");
                    jTextArea6.append("Th:m:s\r\n");
                    if (jRadioButton_Logo.isSelected()) {
                               jTextArea6.append("Y24,8,LD6\r\n");//Pinto el logo si se selecciona la opción (almacenado en memoria de la impresora)
                               jTextArea6.append("Y24,90,GdiWindowTextBox0\r\n");//Pinto el texto de Denox (almacenado en memoria de la impresora)
                    }
                    jTextArea6.append("BE,264,24,3,8,80,0,1," + jTextField_OfEan.getText() +"\r\n");
                    jTextArea6.append("W48,200,5,2,M0,8,4,18,0\r\n");
                    jTextArea6.append(referencia+ "LOT"+ numLote+"\r\n");
                    jTextArea6.append("AB,48,286,1,1,1,0,LOT: "+jTextField_OFarti.getText()+"LOT"+ numLote+"\r\n");
                    jTextArea6.append("AA,272,156,1,1,1,0," + jTextField_OFdescrip.getText()+"\r\n");
                    jTextArea6.append("E\r\n");
                   

                    
                 } else{  //AQUÍ ETIQUETAS DE CAJAS Y PALLETS. ETIQUETAS POR LAS ZEBRA
                    
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
                                        OF = jTextField_OF.getText() + "Q" + padLeft(jFormattedTextField_Pico.getText(),4).replace(" ","0") + " " + padLeft(Integer.toString(bulto),4).replace(" ","0");
                                        System.out.println("OF: " + OF);
                                    }else{
                                       jTextArea6.append("^FDCANTIDAD:" + p_unidades + "^FS\n"); 
                                       OF = jTextField_OF.getText() + "Q" + padLeft(Integer.toString(p_unidades),4).replace(" ","0") + " " + padLeft(Integer.toString(bulto),4).replace(" ","0");
                                       System.out.println("OF: " + OF);
                                    }
                                       
                                }else if (jRadioButton_Caja.isSelected()){
                                    if (Integer.parseInt(jFormattedTextField_PicoCaja.getText())!=0){
                                        System.out.println("pico caja: " + jFormattedTextField_PicoCaja.getText()+ " " + cantxbulto + " " + bulto);
                                        jTextArea6.append("^FDCANTIDAD:" + jFormattedTextField_PicoCaja.getText() + "^FS\n");
                                        OF = jTextField_OF.getText() + "Q" + padLeft(jFormattedTextField_Pico.getText(),4).replace(" ","0") + " " + padLeft(Integer.toString(bulto),4).replace(" ","0");
                                        System.out.println("OF: " + OF);
                                    }else{
                                        jTextArea6.append("^FDCANTIDAD:" + p_unidades + "^FS\n");
                                        OF = jTextField_OF.getText() + "Q" + padLeft(Integer.toString(p_unidades),4).replace(" ","0") + " " + padLeft(Integer.toString(bulto),4).replace(" ","0");
                                    }
                                }
                        }else{
                            jTextArea6.append("^FDCANTIDAD:" + p_unidades + "^FS\n");
                            OF = jTextField_OF.getText() + "Q" + padLeft(Integer.toString(p_unidades),4).replace(" ","0") + " " + padLeft(Integer.toString(bulto),4).replace(" ","0");
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
                jTextArea7.append("^FO20,20^XGE:LD5.JPG,1,1^FS\n");
                jTextArea7.append("^PON\n");
                //jTextArea5.append("^LH0,0\n");
                //jTextArea4.append("^FO20,150\n");
                //jTextArea4.append("^GB760,600,4^FS\n"); //RECUADRO
                jTextArea7.append("^FO50,350\n");
                jTextArea7.append("^FB720,1,,C\n");
                jTextArea7.append("^A0N," + p_unidades + ",\n");
                jTextArea7.append("^FD" + p_codigo + "^FS\n");
                jTextArea7.append("^BY8,3,140\n");
                jTextArea7.append("^FO50,500\n");
                jTextArea7.append("^FB720,1,,C,\n");
                jTextArea7.append("^BCN,,N,N,N,A\n");
                jTextArea7.append("^FD" + p_codigo + "^FS\n");
                jTextArea7.append("^XZ\n");
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
  
    
    //public static void ejecutarCMD(String cmd){
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
        if (jRadioButton_Caja.isSelected() || jRadioButton_Pallet.isSelected() || jRadioButton_Producto.isSelected()) {
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
                            "FROM TEST.STOLOC\n" +
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
                            "FROM TEST.STOLOC\n" +
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
                      + "FROM TEST.STOLOC\n"
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
                      + "FROM TEST.STOLOC\n"
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
    private javax.swing.ButtonGroup buttonGroup_TipoEti;
    private javax.swing.ButtonGroup buttonGroup_Ubicaciones;
    private javax.swing.ButtonGroup buttonGroup_idioma;
    private javax.swing.ButtonGroup buttonGroup_idioma_alb;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton_ConsultaAlbNormal;
    private javax.swing.JButton jButton_ConsultaOF;
    private javax.swing.JButton jButton_RefrescarAlb;
    private javax.swing.JButton jButton_RefrescarAlb1;
    private javax.swing.JButton jButton_UbiConsulta;
    private javax.swing.JButton jButton_consultar;
    private javax.swing.JButton jButton_consultar_registro;
    private javax.swing.JComboBox jComboBox_AlbNormal;
    private javax.swing.JComboBox jComboBox_OF;
    private javax.swing.JComboBox jComboBox_Padres;
    private javax.swing.JComboBox jComboBox_Ubi;
    private javax.swing.JComboBox jCombo_pedpro;
    private javax.swing.JComboBox jCombo_ref;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JDialog jDialog2;
    private javax.swing.JDialog jDialog3;
    private javax.swing.JDialog jDialog4;
    private javax.swing.JDialog jDialog5;
    private javax.swing.JDialog jDialog6;
    private javax.swing.JFormattedTextField jFormattedTextField_CantCaja;
    private javax.swing.JFormattedTextField jFormattedTextField_CantOF;
    private javax.swing.JFormattedTextField jFormattedTextField_CantPadreCaja;
    private javax.swing.JFormattedTextField jFormattedTextField_CantPadrePallet;
    private javax.swing.JFormattedTextField jFormattedTextField_CantPallet;
    private javax.swing.JFormattedTextField jFormattedTextField_CapCaja;
    private javax.swing.JFormattedTextField jFormattedTextField_CapPallet;
    private javax.swing.JFormattedTextField jFormattedTextField_Pico;
    private javax.swing.JFormattedTextField jFormattedTextField_PicoCaja;
    private javax.swing.JFormattedTextField jFormattedTextField_cantidad;
    private javax.swing.JFormattedTextField jFormattedTextField_cantidad1;
    private javax.swing.JFormattedTextField jFormattedTextField_cantidadUbi;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize1;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize2;
    private javax.swing.JFormattedTextField jFormattedTextField_fontSize3;
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
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_albaran;
    private javax.swing.JLabel jLabel_logo2;
    private javax.swing.JLabel jLabel_logo3;
    private javax.swing.JLabel jLabel_logo4;
    private javax.swing.JLabel jLabel_logo5;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JLayeredPane jLayeredPane3;
    private javax.swing.JLayeredPane jLayeredPane4;
    private javax.swing.JLayeredPane jLayeredPane5;
    private javax.swing.JLayeredPane jLayeredPane6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem_OF;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
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
    private javax.swing.JRadioButton jRadioButton_Godex1;
    private javax.swing.JRadioButton jRadioButton_Godex2;
    private javax.swing.JRadioButton jRadioButton_Logo;
    private javax.swing.JRadioButton jRadioButton_OF;
    private javax.swing.JRadioButton jRadioButton_Padre;
    private javax.swing.JRadioButton jRadioButton_Pallet;
    private javax.swing.JRadioButton jRadioButton_Producto;
    private javax.swing.JRadioButton jRadioButton_UbiFam;
    private javax.swing.JRadioButton jRadioButton_UbiTri;
    private javax.swing.JRadioButton jRadioButton_Zebra1;
    private javax.swing.JRadioButton jRadioButton_Zebra2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
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
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextArea jTextArea5;
    private javax.swing.JTextArea jTextArea6;
    private javax.swing.JTextArea jTextArea7;
    private javax.swing.JTextArea jTextArea_NetUse;
    private javax.swing.JTextArea jTextArea_log;
    private javax.swing.JTextField jTextField_AlbBultos;
    private javax.swing.JTextField jTextField_AlbCte;
    private javax.swing.JTextField jTextField_AlbDir2;
    private javax.swing.JTextField jTextField_AlbDirEnv;
    private javax.swing.JTextField jTextField_AlbDirPostal;
    private javax.swing.JTextField jTextField_AlbNomCte;
    private javax.swing.JTextField jTextField_AlbNumAlb;
    private javax.swing.JTextField jTextField_AlbPedidoCte;
    private javax.swing.JTextField jTextField_AlbPrep;
    private javax.swing.JTextField jTextField_AlbTrans;
    private javax.swing.JTextField jTextField_OF;
    private javax.swing.JTextField jTextField_OFarti;
    private javax.swing.JTextField jTextField_OFdescrip;
    private javax.swing.JTextField jTextField_OfEan;
    private javax.swing.JTextField jTextField_OfKit;
    private javax.swing.JTextField jTextField_OfLote;
    private javax.swing.JTextField jTextField_PadreArti;
    private javax.swing.JTextField jTextField_PadreDesc;
    private javax.swing.JTextField jTextField_PadreEan;
    private javax.swing.JTextField jText_UbiDesde;
    private javax.swing.JTextField jText_UbiHasta;
    private javax.swing.JTextField jText_albaran;
    private javax.swing.JTextField jText_empresa;
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
