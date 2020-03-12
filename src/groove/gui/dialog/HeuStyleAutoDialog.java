
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

import groove.verify.HeuStyleAuto;

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


public class HeuStyleAutoDialog extends JDialog  {

	private static final String START_COMMAND = "Intelligent Checking the Selected Large Model";
	private static final String Make_Knowlege_Base_COMMAND = "Make Knowlege Base & Data Mining";
	private static final String Enable_HostGraph_COMMAND = "Enable the Selected HostGraph";
	private static final String Explore_COMMAND = "Explore State Space";
	
	private static final String CANCEL_COMMAND = "Exit";
	
	
    private static final String Generate_COMMAND = "Generate a Smaller Model Automatically and Learning by Data Mining";

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
    public HeuStyleAutoDialog(Simulator simulator, JFrame owner) {
    	
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
        
     
    	
        
        
        Alltype=new ArrayList<QualName>();
		
    	GrammarModel grammermodel=simulator.getModel().getGrammar();
        Set<QualName> sname= grammermodel.getNames(ResourceKind.RULE);
       
       	Iterator<QualName> it=sname.iterator();
       	while(it.hasNext()){
       			QualName ts=it.next();
        		RuleModel rulemodel=grammermodel.getRuleModel(ts);
	        	if(rulemodel.isEnabled()){
	        		Set<? extends AspectEdge> edgeSet=rulemodel.getSource().edgeSet();
	   
	        		boolean flag=false;
	        		for(AspectEdge ae:edgeSet ){
	        			//if(ae.toString().contains("new:") ||ae.toString().contains("del:") || ae.toString().contains("not:") ){
	        			if(ae.toString().contains("new:") ||ae.toString().contains("del:")  ){
	        				flag=true;
	        				break;
	        			}
	        		}
	        		if(!flag){
	        			
	        				try {
								if(rulemodel.toResource().getAnchor().size()>0)
									flag=true;
							} catch (groove.util.parse.FormatException e) {
								e.printStackTrace();
							}
	        			
	        		}
	        		
	        		if(!flag)
	        			Alltype.add(ts);
	        	}
	       	}

     	
       	
      	
        
        
        
        
        JLabel jl=new JLabel("All Host Graphs (select a large model)");
        dialogContent.add(jl);
        
        cmbHostGraph= new JComboBox();
        cmbHostGraph.setBackground(INFO_BOX_BG_COLOR);
        dialogContent.add(cmbHostGraph);
        
        sname= grammermodel.getNames(ResourceKind.HOST);
       	it=sname.iterator();
       	while(it.hasNext())
       	{
       		QualName ts=it.next();
       		cmbHostGraph.addItem(ts);
       	}
     
        
        
       
       
        
       	dialogContent.add(createEnableHostgraphPanel());
       	
       	
       
    	dialogContent.add(createRBmodelcheckingPanel());
    	dialogContent.add(createRBmodelcheckingLivenessPanel());
    	
      
       	
       	
    	
    	
    	dialogContent.add(new JLabel("The state property q:"));
        
    	cmbModelCheckingType=new JComboBox();
    	cmbModelCheckingType.setBackground(INFO_BOX_BG_COLOR);
        dialogContent.add(cmbModelCheckingType);
        
        
        JLabel jstep3=new JLabel("The size of the Generated Smaller Model is what Percentage of the Larger Model (Minpercent)?");
        dialogContent.add(jstep3);
        
        txtpercent=new JTextField("2");
        dialogContent.add(txtpercent);
       
        
        
        
        dialogContent.add(createGeneratedPanel());
    	
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
        
        JLabel jstep5=new JLabel("Total time spent (Generating and exhaustive exploring of the samller model + Executing of the FindFreqPatt algorithm");
        JLabel jstep6=new JLabel("   +Efficient exploring of the large model+...) , Number of explored states");
        jstep5.setForeground(Color.blue);
        jstep6.setForeground(Color.blue);
        dialogContent.add(jstep5);
        dialogContent.add(jstep6);
        
        
        
        txtTimeSpent=new JTextField(" ");
        txtTimeSpent.setForeground(INFO_BOX_BG_COLOR);
        txtTimeSpent.setEnabled(false);
        dialogContent.add(txtTimeSpent);
        
        dialogContent.add(createCancelPanel());
        
       
        
        SpringUtilities.makeCompactGrid(dialogContent, 18, 1, 5, 5, 15, 0);
        // Add the dialogContent to the dialog.
        add(dialogContent);
        setTitle("Model Checking by Data Mining(from exploring of the minimized problem) ...");
        setIconImage(Icons.GROOVE_ICON_16x16.getImage());
        setSize(810, 600);  //width height
        //pack();
        setLocationRelativeTo(owner);
        
        
        
        /////
        EnableHostGraphButton.setEnabled(true);
        GenerateButton.setEnabled(false);
  
       
        startButton.setEnabled(false);
        ////
        
        
        setVisible(true);
        
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
    
    private JPanel createGeneratedPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getGenerateButton());
        return buttonPanel;
    }

 
    
    private JPanel createEnableHostgraphPanel() {
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.add(getEnableHostGraphButton());
        return buttonPanel;
    }
    
   
   
    private JPanel createRBmodelcheckingPanel() {
    	JPanel buttonPanel = new JPanel();
    	rbdeadlock=new JRadioButton("Deadlock (EF q)");
    	rbreachability=new JRadioButton("Reachabiliy (EF q)");
    	rbsafety=new JRadioButton("Refutation of Safety (AG !q)");
    	
    	
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
   	
    	rbsafety.addItemListener(new ItemListener() {
           public void itemStateChanged(ItemEvent e) {             
          	 manageRB();
           }           
        });
    	
    	
    	
    	rbdeadlock.setSelected(true);
    	//rbreachability.setEnabled(false);
    	
    	buttonPanel.add(rbdeadlock);
    	buttonPanel.add(rbreachability);
    	buttonPanel.add(rbsafety);
    	
        return buttonPanel;
    }
    private JPanel createRBmodelcheckingLivenessPanel() {
    	JPanel buttonPanel = new JPanel();
    	
       	rbLivenessDead=new JRadioButton("Refutation of liveness by a path leading to a deadlock");
    	rbLivenessCycle=new JRadioButton("Refutation of liveness by a path leading to a cycle");
    	
    	
    	
    	rbLivenessDead.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
    	 rbLivenessCycle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
    	
    	
    	
    	buttonPanel.add(rbLivenessDead);
    	buttonPanel.add(rbLivenessCycle);
    
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbdeadlock);
    	options.add(rbreachability);
    	options.add(rbsafety);
    	options.add(rbLivenessDead);
    	options.add(rbLivenessCycle);
    	
    	
        return buttonPanel;
    }
    
    
    
    private RefreshButton startButton;
    private RefreshButton cancelButton;
    private RefreshButton EnableHostGraphButton;
    private RefreshButton GenerateButton;
  
    
    
    
    
   
    private Simulator simulator;
    private HeuStyleAuto heuristicreach;
    private JComboBox cmbModelCheckingType;
   
    private  JComboBox cmbHostGraph;
    
    private  JRadioButton rbdeadlock;
    private  JRadioButton rbreachability;
    private  JRadioButton rbsafety;
    private  JRadioButton rbLivenessDead;
    private  JRadioButton rbLivenessCycle;
    
    private JTextField txtresultOfmodelchecking;
    private JTextField txtpercent;
    private JTextField txtTimeSpent;
    ArrayList<QualName> Alltype;
    
    private String ModelCheckingType;   //DeadLock   Reachability  RefuteSafety RefuteLivenessByDead RefuteLivenessByCycle
    private String ModelCheckingTarget;   //property name
    
    
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
    
    
   
    
    /** Initialises and returns the start button. */
    private RefreshButton getStartButton() {
        if (this.startButton == null) {
            
            this.startButton = new RefreshButton(START_COMMAND) {
                @Override
                public void execute() {
                	  
                	if(heuristicreach==null)
                		heuristicreach=new HeuStyleAuto();
                	
               	 	ModelCheckingType="";
               	 	ModelCheckingTarget="";
               	 	   
	               	if(rbreachability.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="Reachability";
	         	    	ModelCheckingTarget=cmbModelCheckingType.getSelectedItem().toString();
	           	 	}
	               	
	               	if(rbdeadlock.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="DeadLock";
	         	    	ModelCheckingTarget="DeadLock";
	           	 	}
	               	
               	   
               	 	if(rbLivenessDead.isSelected()){
            	    	ModelCheckingType="DeadLock";
            	    	ModelCheckingTarget="DeadLock";
            	    }
               	 	if(rbLivenessCycle.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="RefuteLivenessByCycle";
	         	    	ModelCheckingTarget=cmbModelCheckingType.getSelectedItem().toString();
               	 	}
               	 	if(rbsafety.isSelected() ){
	         	    	ModelCheckingType="DeadLock";
	         	    	ModelCheckingTarget="DeadLock";
            	 	}
               	 	
            	    
               	 	heuristicreach.simulator=simulator;
            	 	heuristicreach.ModelCheckingTarget=ModelCheckingTarget;
            	 	heuristicreach.ModelCheckingType=ModelCheckingType;
               	 	
               		setCursor(new Cursor(Cursor.WAIT_CURSOR));
               	 	
               	 	txtresultOfmodelchecking.setText("");
               	 	txtTimeSpent.setText("");
               	 	
                	long startTime = System.currentTimeMillis();
                	String heuristicResult=heuristicreach.start(Integer.parseInt(txtpercent.getText()));
               	    if(heuristicResult==null)
               	    	heuristicResult="noreachability";
               	   
	               	 if(rbdeadlock.isSelected()){
	             		  if(heuristicResult.equals("reachability"))
	                   	   	heuristicResult="The propery is verified.";
	                   	else
	                   	   	heuristicResult="The propery is not verified.";
	             	   }
             	   if(rbreachability.isSelected()){
	           		  if(heuristicResult.equals("reachability"))
	                 	   	heuristicResult="The propery is verified.";
	                 	 else
	                 	   	heuristicResult="The propery is not verified.";
	           	       }
	               	if(rbsafety.isSelected() ){
	           		  if(heuristicResult.equals("reachability"))
	                 	   	heuristicResult="The propery is refuted.";
	                 	 else
	                 	   	heuristicResult="The propery is not refuted.";
	           	   }
	               	if(rbLivenessDead.isSelected() ){
		           		  if(heuristicResult.equals("reachability"))
		                 	   	heuristicResult="The propery is refuted.";
		                 	 else
		                 	   	heuristicResult="The propery is not refuted.";
		           	 }

	               	if(rbLivenessCycle.isSelected() ){
		           		  if(heuristicResult.equals("reachability"))
		                 	   	heuristicResult="The propery is refuted.";
		                 	 else
		                 	   	heuristicResult="The propery is not refuted.";
		           	 }


           	    	
               	    txtresultOfmodelchecking.setText(heuristicResult);
               	    long reportTime= System.currentTimeMillis() - startTime;
               	    
               	    reportTime_Effi_Explo_Large=reportTime;
               	    
               	    AllreportTime=reportTime_Gen_Exhau_Explo_Small+reportTime_Data_Mining+reportTime_EnableHostGraph+reportTime_Effi_Explo_Large;
               	    Number_Explored_States=heuristicreach.Number_Explored_States;
               	    
               	    String S1=String.valueOf(AllreportTime/1000.0);
            	    String S2=String.valueOf(reportTime_Gen_Exhau_Explo_Small/1000.0);
            	    String S3=String.valueOf(reportTime_Data_Mining/1000.0);
            	    String S4=String.valueOf(reportTime_Effi_Explo_Large/1000.0);
            	    String S5=String.valueOf(heuristicreach.Number_Explored_States);
            	                  	    
            	    
            	    txtTimeSpent.setText(S1+" ( "+S2+" + "+S3+" + "+S4+" + ..." +" ) "+" , "+S5);
            	   
               	    
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
    private void manageRB(){
    	if(startButton!=null){
        	cmbModelCheckingType.removeAllItems();
           	cmbModelCheckingType.setEnabled(true);
    	
        	if(rbdeadlock.isSelected() || rbsafety.isSelected()){
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
        	
        	if(rbLivenessDead.isSelected() || rbLivenessCycle.isSelected()){
        		for(int i=0;i<=Alltype.size()-1;i++){
        			String s=Alltype.get(i).toString();
        			if(s.contains("Live") || s.contains("live")){
        				cmbModelCheckingType.addItem(s);
        			}
        				
        		}
        	}
        	
        	if(cmbModelCheckingType.getItemCount()==0){
        		EnableHostGraphButton.setEnabled(true);
     	        GenerateButton.setEnabled(false);
     	        startButton.setEnabled(false);
        	}
        	else{
        		GenerateButton.setEnabled(true);
        	}
        	
	
    	}
    	               	
    	                	

    }
    
    
  
        
    private RefreshButton getEnableHostGraphButton() {
        if (this.EnableHostGraphButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.EnableHostGraphButton = new RefreshButton(Enable_HostGraph_COMMAND) {
                @Override
                public void execute() {
                	  
                	if(heuristicreach==null)
                		heuristicreach=new HeuStyleAuto();
                	
               	 	
               	 	
                	long startTime = System.currentTimeMillis();
            	    
               	    String HostGraphName;
               	    HostGraphName=cmbHostGraph.getSelectedItem().toString();
            	 	heuristicreach.simulator=simulator;
            	 	heuristicreach.HostGraphName=HostGraphName;
            	 	heuristicreach.EnableSelectedHostGraph();
               	 	
            	 	reportTime_EnableHostGraph= System.currentTimeMillis() - startTime;
               	 	
            	 	///////////
            		EnableHostGraphButton.setEnabled(true);
         	        GenerateButton.setEnabled(true);
         	        ///////////
            	 	
               	 	manageRB();
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
    public long reportTime_EnableHostGraph;
    public long reportTime_Data_Mining;  //Executing of the FindFreqPatt algorithm for data mining
    public long reportTime_Gen_Exhau_Explo_Small;   //Generating and Exhaustive exploring of the samller model
    public long reportTime_Effi_Explo_Large;  //Efficient exploring of the large model
    public long Number_Explored_States;   //Number of explored states
    
    
    
    
    
    
    /** Initialises and returns the cancel button. */
    private RefreshButton getCancelButton() {
        if (this.cancelButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.cancelButton = new RefreshButton(CANCEL_COMMAND) {
                @Override
                public void execute() {
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

    private RefreshButton getGenerateButton() {
        if (this.GenerateButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.GenerateButton = new RefreshButton(Generate_COMMAND) {
                @Override
                public void execute() {
                	
                	if(heuristicreach==null)
                		heuristicreach=new HeuStyleAuto();
                	
                	txtresultOfmodelchecking.setText("");
            		txtresultOfmodelchecking.setForeground(Color.black);
                	
                	ModelCheckingType="";
               	 	ModelCheckingTarget="";
               	 	   
	               	if(rbreachability.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="Reachability";
	         	    	ModelCheckingTarget=cmbModelCheckingType.getSelectedItem().toString();
	           	 	}
	               	
	               	if(rbdeadlock.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="DeadLock";
	         	    	ModelCheckingTarget="DeadLock";
	           	 	}
	               	
               	   
               	 	if(rbLivenessDead.isSelected()){
            	    	ModelCheckingType="DeadLock";
            	    	ModelCheckingTarget="DeadLock";
            	    }
               	 	if(rbLivenessCycle.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="RefuteLivenessByCycle";
	         	    	ModelCheckingTarget=cmbModelCheckingType.getSelectedItem().toString();
               	 	}
               	 	if(rbsafety.isSelected() ){
	         	    	ModelCheckingType="DeadLock";
	         	    	ModelCheckingTarget="DeadLock";
            	 	}
               	 	
            	    
               	 	heuristicreach.simulator=simulator;
            	 	heuristicreach.ModelCheckingTarget=ModelCheckingTarget;
            	 	heuristicreach.ModelCheckingType=ModelCheckingType;
               	 	
               		setCursor(new Cursor(Cursor.WAIT_CURSOR));
               	 	
               	 	txtresultOfmodelchecking.setText("");
               	 	txtTimeSpent.setText("");
               	 	
                	
                	
                	boolean result= heuristicreach.Gen_Explore_MakeKnowlege_SmallerModel(Integer.parseInt(txtpercent.getText()));
                	heuristicreach.EnableSelectedHostGraph();
                	
                	reportTime_Gen_Exhau_Explo_Small= heuristicreach.reportTime_Gen_Exhau_Explo_Small;
                	reportTime_Data_Mining=heuristicreach.reportTime_Data_Mining;
                	
                	if(!result){
                		 txtresultOfmodelchecking.setText("The smaller model can not be generated with this value of the minpercent parameter!! please change its value.");
                		 txtresultOfmodelchecking.setForeground(Color.red);
                		 startButton.setEnabled(false);
                	}else{
                		startButton.setEnabled(true);
                	}
                	
                	
                	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

                @Override
                public void refresh(Exploration exploration) {
                    // do nothing
                }
            };
        }
        return this.GenerateButton;
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
