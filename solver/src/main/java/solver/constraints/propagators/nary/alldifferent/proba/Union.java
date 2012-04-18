/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;

/**
 * <br/>
 *
 * @author Xavier Lorca
 * @since 28 nov. 2011
 */
public class Union implements IUnion {

    /**
     * link between a value and its position among all the values
     */
    TIntIntHashMap val2idx;

    /**
     * values in the union from 0 to idx included
     */
    Value[] values;
    IStateInt idx;

    int lastInstValuePos;
    int lastLowValuePos;
    int lastUppValuePos;


    public Union(IntVar[] variables, IEnvironment environment) {
        Set<Value> vals = new HashSet<Value>();
        //Set<Integer> instVals = new HashSet<Integer>();
        TIntObjectHashMap<Value> int2Value = new TIntObjectHashMap<Value>();
        /*for (IntVar var : variables) {
            if (var.instantiated()) {
                instVals.add(var.getValue());
            }
        }*/
        for (IntVar var : variables) {
            int ub = var.getUB();
            for (int value = var.getLB(); value <= ub; value = var.nextValue(value)) {
                //if (!instVals.contains(value)) {
                    Value v = int2Value.get(value);
                    if (v == null) {
                        v = new Value(value, environment);
                        vals.add(v);
                        int2Value.put(value, v);
                    }
                    v.incrOcc();
                //}
            }
        }
        values = vals.toArray(new Value[vals.size()]);
        idx = environment.makeInt(values.length - 1);
        val2idx = new TIntIntHashMap();
        for (int i = 0; i < values.length; i++) {
            val2idx.put(values[i].getValue(), i);
        }
        /*System.out.print("Union incremental: ");
        for (int i = 0; i <= idx.get(); i++) {
            System.out.print(values[i] + ";");
        }
        System.out.println("\n-----------------");//*/
    }

    @Override
    public void remove(int value) {
        int lastPresent = idx.get();
        int indice = val2idx.get(value);
        if (indice <= lastPresent) {
            Value v = values[indice];
            v.decrOcc();
            if (v.getOcc() == 0) {
                Value lastElement = values[lastPresent];
                values[lastPresent] = values[indice];
                values[indice] = lastElement;
                val2idx.adjustValue(lastElement.getValue(),indice-lastPresent);
                val2idx.adjustValue(value,lastPresent-indice);
                idx.add(-1);
            }
        }
    }

    @Override
    public void instantiatedValue(int value, int low, int upp) {
        lastInstValuePos = val2idx.get(value);
        lastLowValuePos = val2idx.get(low);
        lastUppValuePos = val2idx.get(upp);
        remove(value);
    }

    @Override
    public int getLastInstValuePos() {
        return lastInstValuePos;
    }

    @Override
    public int getLastLowValuePos() {
        return lastLowValuePos;
    }

    @Override
    public int getLastUppValuePos() {
        return lastUppValuePos;
    }

    public int getSize() {
        return idx.get()+1;
    }

    public int[] getValues() {
        int[] tmp = new int[idx.get() + 1];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = values[i].getValue();
        }
        return tmp;
    }

    public int getOccOf(int value) {
        int indice = val2idx.get(value);
        if (indice > idx.get()) {
            return 0;
        } else {
            return values[indice].getOcc();
        }
    }

    public String toString() {
        String res = "union : [";
        for (int i = 0; i <= idx.get(); i++) {
            res += values[i].getValue() + ", ";
        }
        res = res.substring(0, res.length() - 2);
        res += "]";
        return res;
    }

}

final class Value {

    private int value;
    private IStateInt occ;

    Value(int value, IEnvironment environment) {
        this.value = value;
        this.occ = environment.makeInt();
    }

    public final int getValue() {
        return value;
    }

    public final int getOcc() {
        return occ.get();
    }

    public final void incrOcc() {
        this.occ.add(1);
    }

    public final void decrOcc() {
        this.occ.add(-1);
    }

    public boolean equals(Object o) {
        Value v = (Value) o;
        return this.value == v.getValue();
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    public String toString() {
        return "<" + value + "," + occ + ">";
    }
}