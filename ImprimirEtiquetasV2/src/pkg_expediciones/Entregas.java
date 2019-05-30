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
                    jText_referencia.setText(a.substring(0, pos));
                    jText_ref_cliente.setText(a.substring(pos + 1, a.length()));
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
        jDialog1.setLocationRelativeTo(this);
        //pongo un título 
        jDialog1.setTitle("Impresión de referencias");
        //pinto pero no va
        //JOptionPane.showMessageDialog(this, "pinto background del dialogo");
        //jDialog1.setBackground(Color.getHSBColor(141,191,72)); //no va ??
    }
    public void DoConnect() {
        /*es necesario meter en try catch las operaciones con conexión a db*/
        try {
            String host = "jdbc:mysql://192.168.35.25:3306/mariodb?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC";
            String uName = "famesa";
            String uPass = "@Famesa123";
            con = DriverManager.getConnection(host, uName, uPass);

            //Statement stmt = con.createStatement();//esta conexión sólo permite navegar con los registros hacia adelante
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            // String query = "SELECT ALBARAN, NUM_BULTO, ARTICULO, PEDIDO_FAM, NOM_CTE, COD_EAN  fROM V_BULTOS_ENTREGAS";
            // rs = stmt.executeQuery(query);//rs contendrá todos los registros

            //while (rs.next()){//el cursor ResultSet apunta justo antes del primer registro cuando los datos se cargan por eso lo primero es mover un regsitro hacia adelante
            // rs.next(); //muevo el cursor al primer registro y muestro los datos en los campos
            /*String referencia = rs.getString("REFERENCIA");
            String nom_cte = rs.getString("NOM_CTE");
            long cod_dun14 = rs.getLong("COD_DUN14");
            String dun14_str = Long.toString(cod_dun14); //convierto long a string
            int cod_cte = rs.getInt("CLIENTE");
            String cod_cte_str = Integer.toString(cod_cte); // convierto int a string*/

            //System.out.println(referencia + " " + nom_cte + " " + cod_dun14 + " " + cod_cte);

            /*textCod_cte.setText(cod_cte_str);
            textDun14.setText(dun14_str);
            textNom_cte.setText(nom_cte);
            textReferencia.setText(referencia);*/

            //}
        } catch (SQLException err) {
            System.out.println(err.getMessage());
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
        jPanel7 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jText_albaran = new javax.swing.JTextField();
        jLabel_albaran = new javax.swing.JLabel();
        jButton_consultar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu_ir = new javax.swing.JMenu();
        jMenuItem_albaranes = new javax.swing.JMenuItem();
        jMenuItem_referencias = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_salir = new javax.swing.JMenuItem();

        jDialog1.setTitle("Referencias");
        jDialog1.setBackground(new java.awt.Color(240, 181, 81));
        jDialog1.setBounds(new java.awt.Rectangle(500, 600, 800, 600));
        jDialog1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jDialog1.setForeground(java.awt.Color.gray);
        jDialog1.setLocation(new java.awt.Point(0, 0));
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

        jButton4.setText("Limpiar");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jRadioButton1.setBackground(jDialog1.getBackground());
        buttonGroup_idioma.add(jRadioButton1);
        jRadioButton1.setSelected(true);
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
                                    .addComponent(jCombo_ref, 0, 319, Short.MAX_VALUE))))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
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
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
            .addComponent(jLabel_logo2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel_albaran, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jText_albaran, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(126, 126, 126)
                .addComponent(jButton_consultar, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(308, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_albaran, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_albaran)
                    .addComponent(jButton_consultar))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(141, 191, 72));

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(240, 240, 240));
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ZPL", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setText("Generar fichero");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(0, 78, Short.MAX_VALUE))
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

        jMenuItem_albaranes.setText("Albaranes");
        jMenu_ir.add(jMenuItem_albaranes);

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

        try {
            String query = "SELECT ALBARAN, NUM_BULTO, ARTICULO, PEDIDO_FAM, NOM_CTE, COD_EAN  fROM V_BULTOS_ENTREGAS WHERE ALBARAN like '" + jText_albaran.getText() + "'";
            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            zpl(1,1,0);
            /*while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en los campos
                String albaran = rs.getString("ALBARAN");
                String num_bulto = rs.getString("NUM_BULTO");
                String articulo = rs.getString("ARTICULO");
                String nom_cte = rs.getString("NOM_CTE");
                String cod_ean = rs.getString("COD_EAN");
                

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
                zpl();
           
                
            }*/
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
    }//GEN-LAST:event_jButton_consultarActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
      generar_fichero (1)   ;
        // Guardo la info en un fichero  
//        String file_name = "C:/TEMPORAL/etiqueta.txt";
//
//        try {
//            WriteFile data = new WriteFile(file_name, false);
//            //data.writeToFile("this is another line of text ñ");
//            data.writeToFile(jTextArea1.getText());
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//        JOptionPane.showMessageDialog(this, "fichero: "+ file_name + " generado");

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // manda orden de impresión al lpt1. La impresora ha de estar mapeada al LPT1
         //System.getProperty("user.dir");
        ejecutarCMD("CMD /C type C:\\tmp\\etiqueta.txt > lpt1");
         
        
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
        
        jDialog1.setVisible(true);
        
        //LLenamos nuestro ComboBox
           
        try {
           
            String query = "select REFERENCIA, CLIENTE, NOM_CTE, DESCRIP, EAN FROM V_ARTI_DUN14";

            rs = stmt.executeQuery(query);//rs contendrá todos los registros
            //zpl();
            
            while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en los campos
                jCombo_ref.addItem(rs.getString("REFERENCIA") + " " + rs.getString("CLIENTE"));

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
                    + "FROM V_ARTI_DUN14_TRAD\n"
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
            zpl(2,Integer.parseInt(jFormattedTextField_cantidad.getText()),0); //le paso la cantidad de etiquetas a imprimir
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
//            String query = "select PEDPRO_NUMPED, PEDPRO_PROV, PEDPRO_RAZON_SOCIAL, PEDPRO_ART, PEDPRO_CANT_UOM, DESCRIPCION, DUN14, CAP_DUN, COD_EAN, DESCRIP\n"
//                    + "From V_PEDIDOS_PROVEEDOR_ART\n"
//                    + "WHERE PEDPRO_NUMPED LIKE '%0156' ";
            String query = "select PEDPRO_NUMPED, PEDPRO_PROV, PEDPRO_RAZON_SOCIAL, PEDPRO_ART, PEDPRO_ART_SIN_COLOR, PEDPRO_CANT_UOM, DESCRIPCION, DUN14, CAP_DUN, COD_EAN, DESCRIP\n"
                    + "From V_PEDIDOS_PROVEEDOR_ART\n"
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
                    + "FROM V_ARTI_DUN14_TRAD\n"
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
                zpl(2,Integer.parseInt(jFormattedTextField_cantidad.getText()),0); //le paso la cantidad de etiquetas a imprimir
                System.out.println(rs.getString("DESCRIP") + " " + rs.getString("CLIENTE") + " " + rs.getString("NOM_CTE") + " " + rs.getString("EAN"));

                //rellenaTextArea(albaran, num_bulto, articulo, nom_cte, cod_ean);
                //zpl();
            }
        } catch (SQLException err) {
            JOptionPane.showMessageDialog(this, err.getMessage());
        }
    }//GEN-LAST:event_jButton_consultar_registroActionPerformed

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
        
        jDialog2.setVisible(true);
        
        //LLenamos nuestro ComboBox con los pedidos a proveedor
           
        try {
           
            String query = "select PEDPRO_NUMPED, PEDPRO_PROV, PEDPRO_RAZON_SOCIAL From V_PEDIDOS_PROVEEDOR_ART GROUP BY PEDPRO_NUMPED, PEDPRO_PROV, PEDPRO_RAZON_SOCIAL";

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
                    zpl(3, cantidad, capacidad_dun);
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
         } else {  //relleno el textArea de pedidos a proveedor

             
             jTextArea3.append(padRight(p_Texto1,20) +  padRight(p_Texto2,18) + padRight(p_Texto3,36)+ padRight(p_Texto4,20)+ p_Texto5);
             jTextArea3.append(System.getProperty("line.separator"));

         }

    }
    public void generar_fichero(int p_TextArea){
           // Guardo la info en un fichero  
        String file_name = "C:/tmp/etiqueta.txt";

        try {
            WriteFile data = new WriteFile(file_name, false);
            //data.writeToFile("this is another line of text ñ");
            if (p_TextArea == 1) {
                data.writeToFile(jTextArea1.getText());
            } else if (p_TextArea == 2){
                data.writeToFile(jTextArea2.getText());
            }else{
                data.writeToFile(jTextArea3.getText());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        JOptionPane.showMessageDialog(this, "fichero: " + file_name + " generado");
    }
         
    public void zpl(int p_modo, int p_num, int p_unidades){

        try {
            if (p_modo == 1) {//rellenar etiquetas para albarán
                while (rs.next()) { //muevo el cursor al primer registro y muestro los datos en los campos
                    String cod_ean = rs.getString("COD_EAN");

                    jTextArea1.append("^XA\n");
                    jTextArea1.append("^PON\n");
                    jTextArea1.append("^LH0,0\n");
                    jTextArea1.append("^BY3,2.5,200\n");
                    jTextArea1.append("^FO150,225\n");
                    jTextArea1.append("^BCB,200,Y,N,N,D\n");
                    jTextArea1.append("^FD(01)" + cod_ean + "^FS\n");
                    jTextArea1.append("^XZ\n");

                }
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
            } else {  //rellenar etiquetas para proveedor
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
                    String ref_sin_color = (String) model.getValueAt(jTb.getSelectedRow(),0);
                    jTextArea3.append("^FD " + "REF: " + ref_sin_color.substring(0,pos_punto(ref_sin_color)) + "^FS\n"); //elimino la parte del color de la referencia
                    jTextArea3.append("^BY3,2.5,100\n");
                    jTextArea3.append("^FO225,530\n");
                    jTextArea3.append("^B2N,,Y,N\n");
                    jTextArea3.append("^FD " + model.getValueAt(jTb.getSelectedRow(), 4) + "^FS\n");
                    jTextArea3.append("^FB450,1,,C,\n");
                    jTextArea3.append("^FO30,200^A0B,40,40^FD " + model.getValueAt(jTb.getSelectedRow(), 5) + "^FS\n");
                    jTextArea3.append("^XZ\n");

                }
                //imprimo las etiquetas DUN14 (para la caja). Etiquetas para caja = p_unidades / capacidad dun
                if (p_unidades > 1){
                System.out.println ("Num etiquetas caja: (" +p_unidades+"/"+p_num + ") "+ Math.floor(p_num/p_unidades));
                for (contador = 0; contador < Math.floor(p_num/p_unidades); contador++) {
                    jTextArea3.append("^XA\n");
                    jTextArea3.append("^PON\n");
                    jTextArea3.append("^LH0,0\n");
                    jTextArea3.append("^FO80,50\n");
                    jTextArea3.append("^FB700,3,,C,\n");
                    jTextArea3.append("^A0N,50,50\n");
                    jTextArea3.append("^FD " + jText_empresa.getText()+ "\n");
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
                    String ref_sin_color = (String) model.getValueAt(jTb.getSelectedRow(),0);
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
  
    
    public static void ejecutarCMD(String cmd){
    Process p;
    try {
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = "";
      while ((line = reader.readLine())!= null) {
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
    private javax.swing.ButtonGroup buttonGroup_idioma;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton_consultar;
    private javax.swing.JButton jButton_consultar_registro;
    private javax.swing.JComboBox jCombo_pedpro;
    private javax.swing.JComboBox jCombo_ref;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JDialog jDialog2;
    private javax.swing.JFormattedTextField jFormattedTextField_cantidad;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_albaran;
    private javax.swing.JLabel jLabel_logo2;
    private javax.swing.JLabel jLabel_logo3;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem_albaranes;
    private javax.swing.JMenuItem jMenuItem_referencias;
    private javax.swing.JMenuItem jMenuItem_salir;
    private javax.swing.JMenu jMenu_ir;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTable jTb;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
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
    // End of variables declaration//GEN-END:variables
}
