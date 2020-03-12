
package groove.gui.dialog;


import groove.explore.Exploration;
import groove.grammar.QualName;
import groove.grammar.aspect.AspectEdge;

import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.model.RuleModel;

import groove.gui.Icons;
import groove.gui.Simulator;
import groove.gui.layout.SpringUtilities;
import groove.util.parse.FormatException;
import groove.verify.HeuPSO;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;


/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */

public class HeuPSODialog extends JDialog  {

	private static final String START_COMMAND = "Start";
	private static final String Make_Knowlege_Base_COMMAND = "Make Knowlege Base";
	private static final String Enable_HostGraph_COMMAND = "Enable the Selected HostGraph";
	private static final String Explore_COMMAND = "Explore State Space";
	
	
    private static final String CANCEL_COMMAND = "Exit";

    private static final String START_TOOLTIP =
        "Restart with the customized exploration";
    
    private static final String Make_knowlege_base_TOOLTIP =
            "Make Knowlege Base...";
    
    
    private static final String Enable_HostGraph_TOOLTIP =
            "Enable The Selected HostGraph...";
    
    private static final String Explore_TOOLTIP =
            "Explore The State Space...";
    
    private static final String DEFAULT_TOOLTIP =
        "Set the currently selected exploration as the default for this grammar";
   

    /**
     * Color to be used for headers on the dialog.
     */
    public static final String HEADER_COLOR = "green";
    /**
     * Color to be used for text in the info panel.
     */
    public static final String INFO_COLOR = "#005050";
    /**
     * Color to be used for the background of the info panel.
     */
    public static final Color INFO_BG_COLOR = new Color(230, 230, 255);
    /**
     * Color to be used for the background boxes on the info panel.
     */
    public static final Color INFO_BOX_BG_COLOR = new Color(210, 210, 255);

    /**
     * Create the dialog.
     * @param simulator - reference to the simulator
     * @param owner - reference to the parent GUI component
     * @throws FormatException
     */
    public HeuPSODialog(Simulator simulator, JFrame owner) {
    	
    	AllreportTime=0;
    	this.simulator=simulator;
    	//Create the content panel.
        JPanel dialogContent = new JPanel(new SpringLayout());
       
        // dialogContent.setBorder(BorderFactory.createEmptyBorder(10, 400, 400, 400));
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        dialogContent.registerKeyboardAction(createCloseListener(), escape,
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogContent.registerKeyboardAction(createCloseListener(), enter,
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        
        ModelCheckingType="Rechability";
        
       
        
        JLabel jl=new JLabel("All Host Graphs");
        dialogContent.add(jl);
        
        cmbHostGraph= new JComboBox();
        cmbHostGraph.setBackground(INFO_BOX_BG_COLOR);
        dialogContent.add(cmbHostGraph);
        
        GrammarModel grammermodel=simulator.getModel().getGrammar();
        Set<QualName> sname= grammermodel.getNames(ResourceKind.HOST);
       	Iterator<QualName> it=sname.iterator();
       	while(it.hasNext())
       	{
       		QualName ts=it.next();
       		cmbHostGraph.addItem(ts);
       	}
     
        
       	dialogContent.add(createEnableHostgraphPanel());
     
       	dialogContent.add(createRBmodelcheckingPanel());
       
       	dialogContent.add(createPSOTypePanel());      	
   
    	
   
    	
    	
       	
        dialogContent.add(createModelChecking());
       	
       	
        
        dialogContent.add(createPSOPanel());
        
        
        
    	
        dialogContent.add(createStartPanel());
        
        
        JLabel line4=new JLabel("------------------------------------------------------------------------------------");
    	line4.setForeground(Color.green);
    	dialogContent.add(line4);
        
        JLabel jstep4=new JLabel("The Result of Model Checking");
        jstep4.setForeground(Color.blue);
        dialogContent.add(jstep4);
        
        txtresultOfmodelchecking=new JTextField("");
        txtresultOfmodelchecking.setForeground(INFO_BOX_BG_COLOR);
        txtresultOfmodelchecking.setEnabled(false);
        dialogContent.add(txtresultOfmodelchecking);
        
        JLabel jstep5=new JLabel("Time Spent, The Number of explored states , The first found goal state depth, The number of fitness function calls");
        //JLabel jstep6=new JLabel("The first deadlock is found after how many repetitions");
        jstep5.setForeground(Color.blue);
        //jstep6.setForeground(Color.blue);
        dialogContent.add(jstep5);
        //dialogContent.add(jstep6);
        
        txtTimeSpent=new JTextField(" ");
        txtTimeSpent.setForeground(INFO_BOX_BG_COLOR);
        txtTimeSpent.setEnabled(false);
        dialogContent.add(txtTimeSpent);
        
        dialogContent.add(createCancelPanel());
        
        manageRB();
        
        SpringUtilities.makeCompactGrid(dialogContent, 14, 1, 5, 5, 15, 0);
        // Add the dialogContent to the dialog.
        add(dialogContent);
        setTitle("Model Checking by PSO Algorithm...");
        setIconImage(Icons.GROOVE_ICON_16x16.getImage());
        setSize(775, 610);  //width  height
        //pack();
        setLocationRelativeTo(owner);
        
        startButton.setEnabled(false);
        
        setVisible(true);
        
     }
    /**
     * Creates the button panel.
     */
    
    
   
    private JPanel createRBmodelcheckingPanel() {
    	JPanel buttonPanel = new JPanel();
    	rbdeadlock=new JRadioButton("Deadlock (EF q)");
    	rbreachability=new JRadioButton("Reachabiliy (EF q)");
    	
    	rbdeadlock.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
   	
	   	rbreachability.addItemListener(new ItemListener() {
	           public void itemStateChanged(ItemEvent e) {             
	          	 manageRB();
	           }           
	     });
    	
    	
    	rbdeadlock.setSelected(true);
    	
    	buttonPanel.add(rbdeadlock);
    	buttonPanel.add(rbreachability);
    	
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbdeadlock);
    	options.add(rbreachability);
    	
    	
    	//////////////////////////////////////
    	//buttonPanel.setVisible(false);
    	///////////////////////////////////////
        return buttonPanel;
    }
    
      
    private int RulesCount;
    public ArrayList<QualName> RulesName;
    
    
    private JPanel createModelChecking(){
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.add(new JLabel("The state property q:"));
        
    	cmbModelCheckingType=new JComboBox();
    	cmbModelCheckingType.setBackground(INFO_BOX_BG_COLOR);
    	buttonPanel.add(cmbModelCheckingType);
    	
    	Alltype=new ArrayList<QualName>();
    	
    	RulesCount=0;
    	RulesName=new ArrayList<QualName>();
    	
    	GrammarModel grammermodel=simulator.getModel().getGrammar();
        Set<QualName> sname= grammermodel.getNames(ResourceKind.RULE);
       
       	Iterator<QualName> it=sname.iterator();
       	while(it.hasNext())
       	{
       			QualName ts=it.next();
        		RuleModel rulemodel=grammermodel.getRuleModel(ts);
	        	if(rulemodel.isEnabled()){
	        		Set<? extends AspectEdge> edgeSet=rulemodel.getSource().edgeSet();
	   
	        		boolean flag=false;
	        		for(AspectEdge ae:edgeSet ){
	        			//	        			if(ae.toString().contains("new:") ||ae.toString().contains("del:") || ae.toString().contains("not:") ){
	        			if(ae.toString().contains("new:") ||ae.toString().contains("del:")  ){
	        				flag=true;
	        				break;
	        			}
	        		}
	        		if(!flag){
	        			try{
	        				if(rulemodel.toResource().getAnchor().size()>0)
	        					flag=true;
	        			}
	        			catch (FormatException e) {
	        				
	        				e.printStackTrace();
	        			}
	        		}
	        		
	        		if(!flag){
	        			cmbModelCheckingType.addItem(ts);
	        			Alltype.add(ts);
	        		}
	        		else{
	        			RulesCount++;
	        			RulesName.add(ts);
	        			
	        		}
	        			
	        	}
	       	}
       		cmbModelCheckingType.addItem("DeadLock");
       		
       		///////////////////////////////
       		//cmbModelCheckingType.setSelectedItem("DeadLock");
       		//cmbModelCheckingType.setEnabled(false);
       		///////////////////////////////
       		
       		return buttonPanel;
    }
    
  
    
    
    private JPanel createStartPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getStartButton());
        return buttonPanel;
    }
    private JPanel createCancelPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getCancelButton());
        return buttonPanel;
    }

   
   
    
    private JPanel createEnableHostgraphPanel() {
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.add(getEnableHostGraphButton());
        return buttonPanel;
    }
    
   
    
    
    
    /**
     * Creates the Genetic panel.
     */
    private JPanel createPSOPanel() {
        JPanel psoPanel = new JPanel(new SpringLayout());
        psoPanel.setBackground(new Color(200, 200, 200));
        
        
        
             
        psoPanel.add(new JLabel("Population"));
        txtPopulation=new JTextField(10);
        txtPopulation.setText("40");
        psoPanel.add(txtPopulation);
        
        
        psoPanel.add(new JLabel("Iterations"));
        txtIterations=new JTextField(10);
        txtIterations.setText("100");
        psoPanel.add(txtIterations);

        psoPanel.add(new JLabel("Depth of Search(the length of each particle)"));
        txtDepthOfSearch=new JTextField(10);
        txtDepthOfSearch.setText("100");
        psoPanel.add(txtDepthOfSearch);

        lblC1=new JLabel("C1");
        psoPanel.add(lblC1);
        txtC1=new JTextField(10);
        txtC1.setText("2.0");
        psoPanel.add(txtC1);

        lblC2=new JLabel("C2");
        psoPanel.add(lblC2);
        txtC2=new JTextField(10);
        txtC2.setText("2.0");
        psoPanel.add(txtC2);
        
        lblW=new JLabel("W");
        psoPanel.add(lblW);
        txtW=new JTextField(10);
        txtW.setText("8.0");
        psoPanel.add(txtW);

        SpringUtilities.makeCompactGrid(psoPanel, 6, 2, 5, 5, 15, 0);
              
        return psoPanel;
    }


    
    private RefreshButton startButton;
    private RefreshButton cancelButton;
    
    private RefreshButton EnableHostGraphButton;

    private RefreshButton EnableHostGraphButton2;
    
    
    private  JRadioButton rbPSO;
    private  JRadioButton rbPSO_GSA;
    
    
    private JTextField  txtPopulation;
    private JTextField txtIterations;
    private JTextField txtDepthOfSearch;
    private JTextField txtC1;
    private JTextField txtC2;
    private JTextField txtW;
    
    private JLabel lblC1;
    private JLabel lblC2;
    private JLabel lblW;
    
    
    private Simulator simulator;
    private HeuPSO heuristicreach;
    private JComboBox cmbModelCheckingType;
    
    private  JComboBox cmbHostGraph;
    
    private  JRadioButton rbdeadlock;
    private  JRadioButton rbreachability;
    private  JRadioButton rbsafety;
    private  JRadioButton rbLiveness;
    
    private JRadioButton rbTruncation;
    private JRadioButton rbTournament;
    
       
    private JTextField txtresultOfmodelchecking;
    private JTextField txtTimeSpent;
    
    private String ModelCheckingType;   //DeadLock   Reach  Safety
    
    //private  JRadioButton rbISreachability;
    //private  JRadioButton rbISsafety;
    
   
    
    ArrayList<QualName> Alltype;
    
    
    
    public void FillModelCheckingType(ArrayList<String> alltype){
    	//cmbModelCheckingType.removeAll();
    	cmbModelCheckingType.removeAllItems();
    	for(int i=0;i<=alltype.size()-1;i++){
    		String s=alltype.get(i);
    		cmbModelCheckingType.addItem(s);
    		cmbModelCheckingType.setSelectedIndex(0);
    	}
    	cmbModelCheckingType.addItem("DeadLock");
    	
    	
    }
   
    private JPanel createPSOTypePanel() {
    	JPanel buttonPanel = new JPanel();
    	
       	
    	rbPSO=new JRadioButton("PSO");
    	rbPSO_GSA=new JRadioButton("PSO-GSA");
    	
    	
    	
    	buttonPanel.add(rbPSO);
    	buttonPanel.add(rbPSO_GSA);
    	
    	rbPSO.setSelected(true);
    	
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbPSO);
    	options.add(rbPSO_GSA);
    	return buttonPanel;
    	
    }
    /** Initialises and returns the start button. */
    private RefreshButton getStartButton() {
        if (this.startButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.startButton = new RefreshButton(START_COMMAND) {
                @Override
                public void execute() {
                	
                	//if(heuristicreach==null)
                		heuristicreach=new HeuPSO();
                	
               	 	String ModelCheckingType;
               	 	ModelCheckingType=cmbModelCheckingType.getSelectedItem().toString();
               	 	
               	 	if(cmbModelCheckingType.getSelectedItem().toString().contains("Live") || cmbModelCheckingType.getSelectedItem().toString().contains("live")){
	         	    	ModelCheckingType="DeadLock";
               	 	}
               	 	
               	    if(rbdeadlock.isSelected()){
               	    	heuristicreach.CTLproperty ="DeadLock";
               	    	heuristicreach.ModelCheckingTarget="DeadLock";
               	    	heuristicreach.ModelCheckingType="DeadLock";
               	    }
               	    if(rbreachability.isSelected()){
            	    	heuristicreach.CTLproperty ="Reachability";
            	    	heuristicreach.ModelCheckingTarget=ModelCheckingType;
            	    }	
               	 	
               	    
               	 if(rbPSO.isSelected())
            	   	heuristicreach.psoType ="PSO";
               	 else
               		heuristicreach.psoType ="PSO-GSA";
            	
               	    
               	 	heuristicreach.simulator=simulator;
               	 
               	    
	               
	               
	               
	             
               	 
	             	
               	 	txtresultOfmodelchecking.setText("");
               	 	txtTimeSpent.setText("");
               	 	
               		setCursor(new Cursor(Cursor.WAIT_CURSOR));
               	 	
                	
                   	heuristicreach.simulator=simulator;
               	 	heuristicreach.CountOFpopulation=Integer.parseInt(txtPopulation.getText());
               	 	heuristicreach.C1=Double.parseDouble(txtC1.getText());
               	 	heuristicreach.DepthOfSearch=Integer.parseInt(txtDepthOfSearch.getText());
               	 	heuristicreach.Iterations=Integer.parseInt(txtIterations.getText());
               	 	heuristicreach.C2=Double.parseDouble(txtC2.getText());
               	 	heuristicreach.W=Double.parseDouble(txtW.getText());
               	 	
               	 	
                	long startTime = System.currentTimeMillis();
               	    String heuristicResult=heuristicreach.start(heuristicreach.ModelCheckingTarget,RulesCount,RulesName,null,null);
               	    
               	    String lastStateInReachability="";
               	    int i=heuristicResult.indexOf("_");
               	    if(i>=0){
               	    	lastStateInReachability=heuristicResult.substring(i+1);
               	    	heuristicResult=heuristicResult.substring(0,i);
               	    }
               	    
               	    boolean flag=false;
               	    if(heuristicResult==null)
               	    	heuristicResult="noreachability";
               	   
               	    if(heuristicResult.equals("reachability"))
               	    	flag=true;
               	    else
               	    	flag=false;
               	    
               	   if(rbdeadlock.isSelected()){
               		  if(heuristicResult.equals("reachability"))
                     	   	heuristicResult="The propery is verified."+"..Last state is: "+lastStateInReachability;
                     	else
                     	   	heuristicResult="The propery is not verified.";
               	   }
               	   if(rbreachability.isSelected()){
             		  if(heuristicResult.equals("reachability"))
                   	   	heuristicResult="The propery is verified."+"..Last state is: "+lastStateInReachability;
                   	 else
                   	   	heuristicResult="The propery is not verified.";
             	   }
	               	               	    
               	    
               	    txtresultOfmodelchecking.setText(heuristicResult);
               	    long reportTime= System.currentTimeMillis() - startTime;
               	    
               	    AllreportTime=reportTime;
               	    
               	    String spenttime= String.valueOf(AllreportTime/1000)+" second :"+ String.valueOf(AllreportTime % 1000) +" millisecond ";
               	    
               	    
               	    String S1=String.valueOf(AllreportTime/1000.0);
               	    String S2=String.valueOf(heuristicreach.Number_Explored_States);
               	    String S3=String.valueOf(heuristicreach.First_Found_Dead_depth+1);
               	    String S4=String.valueOf(heuristicreach.Call_Number_Fitness);
               	 
               	    
               	    if(flag)
               	    	txtTimeSpent.setText(S1+" , " + S2 +" , " +S3+" , " +S4  );
               	    else
               	    	txtTimeSpent.setText("");
               	    
               	    
               	    AllreportTime=0;
               	    
               		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(START_TOOLTIP, exploration);
                }
            };
        }
        return this.startButton;
    }

    /** Initialises and returns the start button. */
    
    private RefreshButton getEnableHostGraphButton() {
        if (this.EnableHostGraphButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.EnableHostGraphButton = new RefreshButton(Enable_HostGraph_COMMAND) {
                @Override
                public void execute() {
                	  
                	if(heuristicreach==null)
                		heuristicreach=new HeuPSO();
                	
               	 	
               	 	
                	long startTime = System.currentTimeMillis();
            	    
               	 	
               	 	
               	 	
                	
               	 	
               	    String HostGraphName;
               	    HostGraphName=cmbHostGraph.getSelectedItem().toString();
            	 	heuristicreach.simulator=simulator;
            	 	heuristicreach.HostGraphName=HostGraphName;
            	 	heuristicreach.EnableSelectedHostGraph();
               	 	

            	 	startButton.setEnabled(true);
               	 	
                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(Enable_HostGraph_TOOLTIP, exploration);
                }
            };
        }
        return this.EnableHostGraphButton;
    }
   
    
    public long AllreportTime;
    
    private void manageRB(){
    	if(startButton!=null){

        	
        	
        	cmbModelCheckingType.removeAllItems();
        	cmbModelCheckingType.setEnabled(true);
    		
        	
        	if(rbdeadlock.isSelected()){
        		cmbModelCheckingType.addItem("DeadLock");
        		cmbModelCheckingType.setEnabled(false);
        		
        	}
        	if(rbreachability.isSelected()){
        		for(int i=0;i<=Alltype.size()-1;i++){
        			String s=Alltype.get(i).toString();
        			if(!s.contains("DeadLock") && !s.contains("Live") && !s.contains("live")){
        				cmbModelCheckingType.addItem(s);
        				
        			}
        				
        		}
        	}
        	
        	
        	if(cmbModelCheckingType.getItemCount()==0)
        		startButton.setEnabled(false);
        	else
        		startButton.setEnabled(true);

        
    	}
    }
    

    /** Initialises and returns the cancel button. */
    private RefreshButton getCancelButton() {
        if (this.cancelButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.cancelButton = new RefreshButton(CANCEL_COMMAND) {
                @Override
                public void execute() {
                	//heuristicreach=null;
                    closeDialog();
                }

                @Override
                public void refresh(Exploration exploration) {
                    // do nothing
                }
            };
        }
        return this.cancelButton;
    }

    
    /**
     * Action that responds to Escape. Ensures that the dialog is closed.
     */
    private ActionListener createCloseListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                closeDialog();
                
            }
        };
    }
    /**
     * The close dialog action. Disposes dialog and resets DismissDelay of the
     * ToolTipManager.
     */
    private void closeDialog() {
        this.dispose();
    }
  
    private abstract class RefreshButton extends JButton {
        /** Constructs a refreshable button with a given button text. */
        public RefreshButton(String text) {
            super(text);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    execute();
                }
            });
        }

        /** Callback action invoked on button click. */
        public abstract void execute();

        /** Callback action allowing the button to refresh its status. */
        public abstract void refresh(Exploration exploration);

        
        protected void setEnabled(String toolTipText, Exploration exploration) {
            setEnabled(true);
       }
    }
    
        
   
}
