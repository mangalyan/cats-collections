package cats.collections

import cats._

/**
 * An intensional set, which is a set which instead of enumerating its elements as a extensional set does, it is defined
 * by a predicate which is a test for membership.
 */
abstract class Predicate[-A] extends scala.Function1[A, Boolean] { self =>

  /**
   * returns a predicate which is the union of this predicate and another
   */
  def union[B <: A](other: Predicate[B]): Predicate[B] = Predicate(a => apply(a) || other(a))

  /**
   * returns a predicate which is the union of this predicate and another
   */
  def |[B <: A](other: Predicate[B]): Predicate[B] = self.union(other)

  /**
   * returns a predicate which is the intersection of this predicate and another
   */
  def intersection[B <: A](other: Predicate[B]): Predicate[B] = Predicate(a => apply(a) && other(a))

  /**
   * returns a predicate which is the intersection of this predicate and another
   */
  def &[B <: A](other: Predicate[B]): Predicate[B] = self.intersection(other)

  /**
   * Returns true if the value satisfies the predicate.
   */
  def contains(a: A): Boolean = apply(a)

  /**
   * Returns the predicate which is the the difference of another predicate removed from this predicate
   */
  def diff[B <: A](remove: Predicate[B]): Predicate[B] = Predicate(a => apply(a) && !remove(a))

  /**
   * Returns the predicate which is the the difference of another predicate removed from this predicate
   */
  def -[B <: A](remove: Predicate[B]): Predicate[B] = self.diff(remove)

  /**
   * Return the opposite predicate
   */
  def negate: Predicate[A] = Predicate(a => !apply(a))

  /**
   * Return the opposite predicate
   */
  def unary_! : Predicate[A] = negate
}

object Predicate extends PredicateInstances {
  def apply[A](f: A => Boolean): Predicate[A] = new Predicate[A] {
    def apply(a: A) = f(a)
  }

  def empty: Predicate[Any] = apply(_ => false)
}

trait PredicateInstances {
  implicit def predicateContravariantMonoidal: ContravariantMonoidal[Predicate] = new ContravariantMonoidal[Predicate] {
    override def contramap[A, B](fb: Predicate[A])(f: B => A): Predicate[B] =
      Predicate(f.andThen(fb.apply))
    override def product[A, B](fa: Predicate[A], fb: Predicate[B]): Predicate[(A, B)] =
      Predicate(v => fa(v._1) || fb(v._2))
    override def unit: Predicate[Unit] = Predicate.empty
  }

  implicit def predicateMonoid[A]: Monoid[Predicate[A]] = new Monoid[Predicate[A]] {
    override def empty: Predicate[A] = Predicate.empty
    override def combine(l: Predicate[A], r: Predicate[A]): Predicate[A] = l.union(r)
  }

  implicit val predicateMonoidK: MonoidK[Predicate] = new MonoidK[Predicate] {
    override def empty[A]: Predicate[A] = Predicate.empty
    override def combineK[A](l: Predicate[A], r: Predicate[A]): Predicate[A] = l.union(r)
  }
}
