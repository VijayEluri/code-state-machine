package com.laamella.code_state_machine.io

import com.laamella.code_state_machine.StateMachine
import com.laamella.code_state_machine.Transition

/**
 * Creates a simple "dot" diagram of the state machine. Start states are double
 * circles, end states are dotted circles, entry and exit events are not shown.
 */
class DotOutput[T, E, P <: Ordered[P]] {
	def getOutput(machine: StateMachine[T, E, P] ): String = {
		val internals = new machine.Internals()

		val output = new StringBuilder()
		output.append("digraph finite_state_machine {\n")
		output.append("\trankdir=LR;\n")
		output.append("\tsize=\"8,5\"\n")
		if (internals.getStartStates.nonEmpty) {
			output.append("\tnode [shape = doublecircle, style=solid];")
			for (startState <- internals.getStartStates) {
				output.append(" " + startState)
			}
			output.append(";\n")
		}
		if (internals.getEndStates.nonEmpty) {
			output.append("\tnode [shape = circle, style=dotted];")
			for (startState <- internals.getEndStates) {
				output.append(" " + startState)
			}
			output.append(";\n")
		}
		output.append("\tnode [shape = circle, style=solid];\n")
		for (sourceState <- internals.getSourceStates) {
			for (transition <- internals.getTransitionsForSourceState(sourceState)) {
				output.append("\t" + sourceState + " -> " + transition.destinationState)
				output.append(" [ label = \"" + transition.conditions + "\" ]")
				output.append(";\n")
			}
		}
		output.append("}\n")
		output.toString()
	}
}