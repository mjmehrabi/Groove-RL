package groove.gui.action;

import groove.grammar.model.GrammarModel;

import groove.gui.Options;
import groove.gui.Simulator;

import groove.gui.dialog.HeuStyleInDialog;




/** Action to open the Exploration Dialog. */
public class HeuStyleInDialogAction extends SimulatorAction {
    /** Constructs an instance of the action. */
    public HeuStyleInDialogAction(Simulator simulator) {
        super(simulator, Options.HeuristicReachStyle_reach_DIALOG_ACTION_NAME,
           null);
    }

    @Override
    public void execute() {
        new HeuStyleInDialog(getSimulator(), getFrame());
       
    }

    
    
    @Override
    public void refresh() {
        GrammarModel grammar = getSimulatorModel().getGrammar();
        setEnabled(grammar != null && !grammar.hasErrors()
            && grammar.hasRules());
    }
}