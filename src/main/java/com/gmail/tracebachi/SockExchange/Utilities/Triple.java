/*
 * SockExchange - Server and Client for BungeeCord and Spigot communication
 * Copyright (C) 2017 tracebachi@gmail.com (GeeItsZee)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.SockExchange.Utilities;

import java.util.Map;
import java.util.Objects;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class Triple<L, M, R>
{
  private final L left;
  private final M middle;
  private final R right;

  /**
   * Constructs a new Triple from the passed left, middle, and right values
   *
   * @param left Value for the left
   * @param middle Value for the middle
   * @param right Value for the right
   */
  public Triple(L left, M middle, R right)
  {
    this.left = left;
    this.middle = middle;
    this.right = right;
  }

  /**
   * Factory method for creating a {@link Pair}
   *
   * @param left Value for the left
   * @param middle Value for the middle
   * @param right Value for the right
   * @param <L> Type of the left
   * @param <M> Type of the middle
   * @param <R> Type of the right
   *
   * @return A Pair containing the left and right value
   */
  public static <L, M, R> Triple<L, M, R> of(L left, M middle, R right)
  {
    return new Triple<>(left, middle, right);
  }

  /**
   * @return Left part of the Triple
   */
  public L getLeft()
  {
    return left;
  }

  /**
   * @return Middle part of the Triple
   */
  public M getMiddle()
  {
    return middle;
  }

  /**
   * @return Right part of the Triple
   */
  public R getRight()
  {
    return right;
  }

  /**
   * See {@link Map.Entry#hashCode()}.
   *
   * @return Integer hashcode
   */
  @Override
  public int hashCode()
  {
    return (left == null ? 0 : left.hashCode()) ^ (middle == null ? 0 : middle
      .hashCode()) ^ (right == null ? 0 : right.hashCode());
  }

  /**
   * Compares if the passed object is equal to the current Triple
   *
   * @param obj Object to compare
   *
   * @return True if equal or false if not
   */
  @Override
  public boolean equals(Object obj)
  {
    return (obj instanceof Triple) && equals((Triple) obj);
  }

  /**
   * @return String representation of this Pair
   */
  @Override
  public String toString()
  {
    return "(" + left + "," + middle + "," + right + ")";
  }

  /**
   * Compares if the passed Pair is equal to the current Pair
   * <p>
   * <p>
   * Two Triples are only equal if both the left, middle, and right of
   * the current Triple are equal to the left and right of the Triple
   * being compared.
   * </p>
   *
   * @param triple Triple to compare
   *
   * @return True if equal or false if not
   */
  public boolean equals(Triple triple)
  {
    return triple != null && Objects.equals(this.left, triple.left) && Objects
      .equals(this.middle, triple.middle) && Objects.equals(this.right, triple.right);
  }
}
