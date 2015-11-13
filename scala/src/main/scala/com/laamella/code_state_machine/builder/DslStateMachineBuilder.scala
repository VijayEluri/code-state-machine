package com.laamella.code_state_machine.builder

import com.laamella.code_state_machine._
import com.laamella.code_state_machine.action.LogAction
import com.laamella.code_state_machine.condition.{AfterCondition, AlwaysCondition, MultiEventMatchCondition, NeverCondition, SingleEventMatchCondition, StatesActiveCondition, StatesInactiveCondition}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
 * A pretty "DSL" builder for a state machine.
 */
abstract class DslStateMachineBuilder[T, E, P <: Ordered[P]](defaultPriority: P) extends StateMachineBuilder[T, E, P] {
  private val log = LoggerFactory.getLogger("DslStateMachineBuilder")

  class DefiningState(sourceStates: mutable.Set[T], internals: StateMachine[T, E, P]#Internals) {
    //		private Set<T> sourceStates = new HashSet<T>();
    //		private final StateMachine<T, E, P>.Internals internals;
    //
    //		public DefiningState() {
    //			this.sourceStates = sourceStates;
    //			this.internals = internals;
    //		}

    def except(states: T*): DefiningState = {
      for (state <- states) {
        sourceStates.remove(state)
      }
      this
    }

    def onExit(action: Action*): DefiningState = {
      for (sourceState <- sourceStates) {
        internals.addExitActions(sourceState, action)
      }
      this
    }

    def onEntry(action: Action*): DefiningState = {
      for (sourceState <- sourceStates) {
        log.debug(s"Create entry action for $sourceState $action")
        internals.addEntryActions(sourceState, action);
      }
      this
    }

    def isAnEndState(): DefiningState = {
      for (state <- sourceStates) {
        internals.addEndState(state)
      }
      this
    }

    def isAStartState(): DefiningState = {
      for (state <- sourceStates) {
        internals.addStartState(state)
      }
      this
    }

    def areEndStates(): DefiningState = {
      isAnEndState()
    }

    def areStartStates(): DefiningState = isAStartState()

    def whenConditions(condition: Condition[E]*): DefiningTransition = {
      //	TODO		assert condition != null;
      new DefiningTransition(sourceStates, new Conditions[E](condition: _*), internals)
    }

    def whenEvents(events: E*): DefiningTransition = {
      new DefiningTransition(sourceStates, is(events:_*), internals)
    }

  }

  class DefiningTransition(sourceStates: mutable.Set[T], conditions: Conditions[E], internals: StateMachine[T, E, P]#Internals) {
    private val actions = new Actions()
    private var priority = defaultPriority

    //		public DefiningTransition() {
    //			this.sourceStates = sourceStates;
    //			this.conditions = conditions;
    //			this.internals = internals;
    //		}

    def action(action: Action): DefiningTransition = {
      //	TODO		assert action != null;
      actions.add(action)
      this
    }

    def then(destinationState: T): DefiningState = {
      //	TODO		assert destinationState != null;
      transition(destinationState, conditions, priority, actions)
    }

    def transition(destinationState: T, storedConditions2: Conditions[E], priority: P, actions: Actions): DefiningState = {
      this.actions.add(actions)
      for (sourceState <- sourceStates) {
        internals.addTransition(new Transition[T, E, P](sourceState, destinationState, storedConditions2, priority, this.actions))
      }
      new DefiningState(sourceStates, internals)
    }

    def transition(destinationState: T, condition: Condition[E], priority: P, actions: Action*): DefiningState = {
      transition(destinationState, new Conditions[E](condition), priority, new Actions(actions: _*))
    }

    def withPrio(priority: P): DefiningTransition = {
      //TODO			assert priority != null;
      this.priority = priority
      this
    }
  }

  private var machine: StateMachine[T, E, P] = _

  override def build(newMachine: StateMachine[T, E, P]): StateMachine[T, E, P] = {
    //	TODO	assert newMachine != null;
    machine = newMachine
    executeBuildInstructions()
    machine
  }

  // TODO see if we can change this pattern to get rid of the var machine
  protected def executeBuildInstructions(): Unit

  override def build(): StateMachine[T, E, P] = {
    build(new StateMachine[T, E, P]())
  }

  def state(state: T): DefiningState = {
    //TODO		assert state != null;
    states(state)
  }

  def states(states: T*): DefiningState = {
    val m = machine;
    new DefiningState(mutable.HashSet[T](states: _*), new m.Internals())
  }

  def active(statesThatMustBeActive: T*): Condition[E] = {
    new StatesActiveCondition[T, E, P](machine, statesThatMustBeActive: _*)
  }

  def inactive(statesThatMustBeInactive: T*): Condition[E] = {
    new StatesInactiveCondition[T, E, P](machine, statesThatMustBeInactive: _*)
  }

  def always() = new AlwaysCondition[E]()



  def never() = new NeverCondition[E]()

  def after(milliseconds: Long) = new AfterCondition[E](milliseconds)

  def is(events: E*): Conditions[E] = {
    //TODO					assert events != null;
    //					assert events.length != 0;
    if (events.length == 1) {
      val singleEvent = events(0)
      return new Conditions[E](new SingleEventMatchCondition[E](singleEvent))
    }
    new Conditions[E](new MultiEventMatchCondition[E](events: _*))
  }

  def log(logText: String): Action = {
    //TODO	assert logText != null;
    new LogAction(logText)
  }

}
