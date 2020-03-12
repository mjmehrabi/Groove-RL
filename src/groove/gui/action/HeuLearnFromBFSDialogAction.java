package groove.gui.action;

import groove.grammar.model.GrammarModel;

import groove.gui.Options;
import groove.gui.Simulator;

import groove.gui.dialog.HeuLearnFromBFSDialog;



/** Action to open the Exploration Dialog. */
public class HeuLearnFromBFSDialogAction extends SimulatorAction {
    /** Constructs an instance of the action. */
    public HeuLearnFromBFSDialogAction(Simulator simulator) {
        super(simulator, Options.HeuristicReachabilityDM_DIALOG_ACTION_NAME,
           null);
    }

    @Override
    public void execute() {
        new HeuLearnFromBFSDialog(getSimulator(), getFrame());
       
    }

    
    
    @Override
    public void refresh() {
        GrammarModel grammar = getSimulatorModel().getGrammar();
        setEnabled(grammar != null && !grammar.hasErrors()
            && grammar.hasRules());
    }
}