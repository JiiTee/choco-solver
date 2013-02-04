/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables;

import choco.kernel.memory.setDataStructures.ISet;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.variables.fast.BitsetIntVarImpl;
import solver.variables.fast.BooleanBoolVarImpl;
import solver.variables.fast.IntervalIntVarImpl;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public enum VariableFactory {
    ;

    //TODO : build domain in Variable

    private static void checkIntVar(String name, int min, int max) {
        if (min - Integer.MIN_VALUE == 0 || max - Integer.MAX_VALUE == 0) {
            throw new SolverException(name + ": consider reducing the bounds to avoid unexpected results");
        }
        if (max < min) {
            throw new SolverException(name + ": wrong domain definition, lower bound > upper bound");
        }
    }

    private static void checkRealVar(String name, double min, double max) {
        if (max < min) {
            throw new SolverException(name + ": wrong domain definition, lower bound > upper bound");
        }
    }

    /**
     * Build a boolean variable, ie domain is [0,1]
     *
     * @param name   name of variable
     * @param solver solver involving the variable
     * @return a BoolVar
     */
    public static BoolVar bool(String name, Solver solver) {
        BooleanBoolVarImpl var = new BooleanBoolVarImpl(name, solver);
        //var.setHeuristicVal(HeuristicValFactory.presetI(var));
        return var;
    }

    public static BoolVar[] boolArray(String name, int size, Solver solver) {
        BoolVar[] vars = new BoolVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = bool(name + "_" + i, solver);
        }
        return vars;
    }

    public static BoolVar[][] boolMatrix(String name, int dim1, int dim2, Solver solver) {
        BoolVar[][] vars = new BoolVar[dim1][];
        for (int i = 0; i < dim1; i++) {
            vars[i] = boolArray(name + "_" + i, dim2, solver);
        }
        return vars;
    }

    public static IntVar bounded(String name, int min, int max, Solver solver) {
        checkIntVar(name, min, max);
        if (min == max) {
            return Views.fixed(name, min, solver);
        } else if (min == 0 && max == 1) {
            return new BooleanBoolVarImpl(name, solver);
        } else {
            return new IntervalIntVarImpl(name, min, max, solver);
        }
    }

    public static IntVar[] boundedArray(String name, int size, int min, int max, Solver solver) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = bounded(name + "_" + i, min, max, solver);
        }
        return vars;
    }

    public static IntVar[][] boundedMatrix(String name, int dim1, int dim2, int min, int max, Solver solver) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = boundedArray(name + "_" + i, dim2, min, max, solver);
        }
        return vars;
    }

    public static IntVar enumerated(String name, int min, int max, Solver solver) {
        checkIntVar(name, min, max);
        if (min == max) {
            return Views.fixed(name, min, solver);
        } else if (min == 0 && max == 1) {
            return new BooleanBoolVarImpl(name, solver);
        } else {
            return new BitsetIntVarImpl(name, min, max, solver);
        }
    }

    public static IntVar[] enumeratedArray(String name, int size, int min, int max, Solver solver) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = enumerated(name + "_" + i, min, max, solver);
        }
        return vars;
    }

    public static IntVar[][] enumeratedMatrix(String name, int size1, int size2, int min, int max, Solver solver) {
        IntVar[][] vars = new IntVar[size1][size2];
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                vars[i][j] = enumerated(name + "_" + i + "_" + j, min, max, solver);
            }
        }
        return vars;
    }

    public static IntVar enumerated(String name, int[] values, Solver solver) {
        checkIntVar(name, values[0], values[values.length - 1]);
        if (values.length == 1) {
            return Views.fixed(name, values[0], solver);
        } else {
            BitsetIntVarImpl var = new BitsetIntVarImpl(name, values, solver);
            //var.setHeuristicVal(HeuristicValFactory.presetI(var));
            return var;
        }
    }

    public static IntVar[] enumeratedArray(String name, int size, int[] values, Solver solver) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = enumerated(name + "_" + i, values, solver);
        }
        return vars;
    }

    public static IntVar[][] enumeratedMatrix(String name, int dim1, int dim2, int[] values, Solver solver) {
        IntVar[][] vars = new IntVar[dim1][];
        for (int i = 0; i < dim1; i++) {
            vars[i] = enumeratedArray(name + "_" + i, dim2, values, solver);
        }
        return vars;
    }

    public static RealVar real(String name, double min, double max, double precision, Solver solver) {
        checkRealVar(name, min, max);
        RealVar var = new RealVarImpl(name, min, max, precision, solver);
        //var.setHeuristicVal(HeuristicValFactory.presetI(var));
        return var;
    }

    public static RealVar[] realArray(String name, int size, double min, double max, double precision, Solver solver) {
        RealVar[] vars = new RealVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = real(name + "_" + i, min, max, precision, solver);
        }
        return vars;
    }

    public static RealVar[][] realMatrix(String name, int dim1, int dim2, double min, double max, double precision, Solver solver) {
        RealVar[][] vars = new RealVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = realArray(name + "_" + i, dim2, min, max, precision, solver);
        }
        return vars;
    }


    public static IntVar[] toIntVar(Variable... variables) {
        IntVar[] ivars = new IntVar[variables.length];
        for (int i = 0; i < variables.length; i++) {
            ivars[i] = (IntVar) variables[i];
        }
        return ivars;
    }


    public static IntVar[][] toMatrix(IntVar[] vars, int dim1, int dim2) {
        IntVar[][] mat = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                mat[i][j] = vars[i + j * dim1];
            }
        }
        return mat;
    }

    public static Task task_bounded(String name, int est, int lst, int minD, int maxD, int ect, int lct, Solver solver) {
        final IntVar start = bounded(name + "_start", est, lst, solver);
        final IntVar duration = bounded(name + "_duration", minD, maxD, solver);
        final IntVar end = bounded(name + "_end", ect, lct, solver);
        return task(start, duration, end);
    }


    public static Task task_enumerated(String name, int est, int lst, int minD, int maxD, int ect, int lct, Solver solver) {
        final IntVar start = enumerated(name + "_start", est, lst, solver);
        final IntVar duration = enumerated(name + "_duration", minD, maxD, solver);
        final IntVar end = enumerated(name + "_end", ect, lct, solver);
		return task(start, duration, end);
    }

	public static Task task(IntVar start, IntVar duration, IntVar end){
		return new Task(start,duration,end);
	}

	// SETS
	public static SetVar set(String name, Solver solver){
		return new SetVarImpl(name,solver) ;
	}

	public static SetVar set(String name, ISet envelop, ISet kernel, Solver solver){
		SetVar s = set(name,solver);
		for(int i=envelop.getFirstElement();i>=0;i=envelop.getNextElement()){
			s.getEnvelope().add(i);
		}
		if(kernel!=null)
			for(int i=kernel.getFirstElement();i>=0;i=kernel.getNextElement()){
				s.getKernel().add(i);
			}
		return s;
	}
}
