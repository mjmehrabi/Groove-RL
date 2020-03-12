package groove.gui.action;

import groove.grammar.model.GrammarModel;

import groove.gui.Options;
import groove.gui.Simulator;
import groove.gui.dialog.HeuStyleInDialog;
import groove.gui.dialog.HeuStyleUserDialog;



/** Action to open the Exploration Dialog. */
public class HeuStyleDialogAction extends SimulatorAction {
    /** Constructs an instance of the action. */
    public HeuStyleDialogAction(Simulator simulator) {
        super(simulator, Options.HeuristicReachabilityLE_DIALOG_ACTION_NAME,
           null);
    }

    @Override
    public void execute() {
        //new HeuStyleUserDialog(getSimulator(), getFrame());
    	new HeuStyleInDialog(getSimulator(), getFrame());
       
    }

    
    
    @Override
    public void refresh() {
        GrammarModel grammar = getSimulatorModel().getGrammar();
        setEnabled(grammar != null && !grammar.hasErrors()
            && grammar.hasRules());
    }
}