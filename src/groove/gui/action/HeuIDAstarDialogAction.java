package groove.gui.action;

import groove.grammar.model.GrammarModel;

import groove.gui.Options;
import groove.gui.Simulator;

import groove.gui.dialog.HeuIDAstarDialog;




/** Action to open the Exploration Dialog. */
public class HeuIDAstarDialogAction extends SimulatorAction {
    /** Constructs an instance of the action. */
    public HeuIDAstarDialogAction(Simulator simulator) {
        super(simulator, Options.HeuIDAstar_DIALOG_ACTION_NAME,
           null);
    }

    @Override
    public void execute() {
        new HeuIDAstarDialog(getSimulator(), getFrame());
       
    }

    
    
    @Override
    public void refresh() {
        GrammarModel grammar = getSimulatorModel().getGrammar();
        setEnabled(grammar != null && !grammar.hasErrors()
            && grammar.hasRules());
    }
}