package info3.game.controller;

import info3.game.controller.Actions.Action;
import info3.game.model.Entities.Entity;

public class BufferAction {
    /* ici on construit un buffer d'action pour permettre au moteur de resoudre toutes les actions en meme temps 
     * (et donc garder le "rythme" ou la "synchroniqation").
    */
    Action action[];
    int size;
    int index;
    public Entity main_entity; //TODO: A supprimer

    /* Prend en paramètre la taille du buffer a construire.
     * Le buffer doit avoir autant d'action que d'entité qui existe sur le moteur ou qu'il y a a faire.
     * Il est possible de faire un buffer plus grand que le nombre d'entité, mais pas plus petit car on y perdrais des actions.
    */

    public BufferAction(int size) {
        this.size = size;
        this.action = new Action[size];
        this.index = 0;
    }

    /* Ajoute une action au buffer.
     * Prend en paramètre une action.
     * Si Action=null, buffer inchangé.
     * Si buffer plein, action non ajoutée.
    */
    public void addAction(Action action) {
        if (action != null) {
            if (index < size) {
                this.action[index] = action;
                index++;
            }
        }
    }

    /* Resout toutes les actions du buffer.
     * Chaque action est resolue sur son entité d'origine.
     * ( possibilité d'ajouté une entité cible en paramètre pour resoudre les actions sur une entité spécifique)
     * 
    */

    public void resolve() {
        for (int i = 0; i < index; i++) {
            if (action[i].e_or == null)
                action[i].exec(main_entity); //TODO: A supprimer
            else
                action[i].exec(action[i].e_or);
        }
        index = 0;
    }
}