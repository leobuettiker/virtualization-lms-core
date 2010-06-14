package test3

import test1._
import test2._


trait Effects extends Base {
  
  type State
  type Effectful[A]
  
  def noEffect: State
  def bindEffect[A](x: State, y: Rep[A]): State
  def reifyState[A](x: Rep[A], y: State): Rep[Effectful[A]]

  var context: State = _
  
  def reflectEffect[A](x: Rep[A]): Rep[A] = {
    context = bindEffect(context, x)
    x
  }
  
  def reifyEffects[A](block: => Rep[A]): Rep[Effectful[A]] = {
    val save = context
    context = noEffect
    
    val result = block
    val resultR = reifyState(result, context)
    context = save
    resultR
  }
  
  
}


trait Effects2 extends Effects {

/*  
    abstract class State
    
    case Object

    def noEffect: Rep[State] = Const(NoEffect)

    case class Bind[A](x: Rep[Effect], y: Rep[A]) extends Def[Effect]

    var context: Rep[State] = noEffect

    def bindEffect[A](x: Rep[Effect], y: Rep[A]): Rep[Effect] = x match {
  //    case Const(NoEffect) => Bind(x, y)
      case _ => Bind(x, y)
    }

    abstract class Effectful[A]

    case class ReifiedEffects[A](x: Rep[A], y: Rep[Effect]) extends Exp[Effectful[A]]
    case class Pure[A](x: Rep[A]) extends Exp[Effectful[A]]
*/  
}





trait Control extends Effects with BaseExp {
  
  case class OrElse[A](x: List[Rep[Effectful[A]]]) extends Def[A]
  
//  def orElse[A](xs: List[Rep[Effectful[A]]]): Rep[A] = reflectEffect(OrElse(xs))
  def orElse[A](xs: List[Rep[Effectful[A]]]): Rep[A] = OrElse(xs)
  
  // OrElse will be pure if all branches contain only match effects!!
  // if any branch contains output, OrElse will be impure
  // (not yet implemented)
  
  // stuff below could be separated
  
  type State = List[Rep[_]]
  abstract class Effectful[A]
  
  case class Reify[A](x: Rep[A], es: List[Rep[_]]) extends Def[Effectful[A]]
  case class Pure[A](x: Rep[A]) extends Exp[Effectful[A]]
  
  def noEffect: State = Nil
  def bindEffect[A](x: State, y: Rep[A]): State = x:::List(y)
  def reifyState[A](x: Rep[A], y: State): Rep[Effectful[A]] = y match {
    case Nil => Pure(x)
    case _ => Reify(x, y)
  }
}

/*
trait ControlOpt extends Control {
  
  abstract class Branch
  
  case class IfTrue(x: Rep[_]) extends Branch
  case class IfFalse(x: Rep[_]) extends Branch

  case class State(pred: List[Branch], yes: List[Branch], no: List[Branch]) extends Def[Effect]
  
  def getState()
  
  def orElse[A](xs: List[Rep[Effectful[A]]]): Rep[A] = {

      val State(p0,p1,p2) = getState()

      var pred: List[Branch] = Nil
      var callYes: List[Branch] = Nil
      var callNo: List[Branch] = Nil
      
      for (ReifiedEffects(_, Def(State(pred1, yes1, no1)) <- xs) {
      }
      
  }

  
  override def bind[A](x: Rep[Effect], y: Rep[A]): Rep[Effect] = {
    x match {
      case Const(NoEffect) => Effects(y)
      case Def(Effects(xs @ _*)) => Effects((xs.toList:::List(y)):_*)
    }
  }
  
}
*/


trait Functions extends Base {
  
  def doLambda[A,B](fun: Rep[A] => Rep[B]): Rep[A => B]
  def doApply[A,B](fun: Rep[A => B], arg: Rep[A]): Rep[B]
  
}

trait FunctionExp extends Functions with BaseExp {
    
  case class Lambda[A,B](fun: Exp[A] => Exp[B]) extends Def[A => B]
  case class Apply[A,B](fun: Exp[A => B], arg: Exp[A]) extends Def[B]
  
  def doLambda[A,B](fun: Exp[A] => Exp[B]): Exp[A => B] = Lambda(fun)
  def doApply[A,B](fun: Exp[A => B], arg: Exp[A]): Exp[B] = Apply(fun, arg)

}

trait FunctionExpUnfoldAll extends FunctionExp {

  override def doApply[A,B](fun: Exp[A => B], arg: Exp[A]): Exp[B] = fun match {
    case Def(Lambda(fun)) => fun(arg)
    case _ => super.doApply(fun, arg)
  }

}


