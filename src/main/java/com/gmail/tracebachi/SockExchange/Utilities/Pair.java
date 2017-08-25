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
public class Pair<L, R>
{
  private final L left;
  private final R right;

  /**
   * Constructs a new Pair from the passed left and right values
   *
   * @param left Value for the left
   * @param right Value for the right
   */
  public Pair(L left, R right)
  {
    this.left = left;
    this.right = right;
  }

  /**
   * Factory method for creating a {@link Pair}
   *
   * @param left Value for the left
   * @param right Value for the right
   * @param <L> Type of the left
   * @param <R> Type of the right
   *
   * @return A Pair containing the left and right value
   */
  public static <L, R> Pair<L, R> of(L left, R right)
  {
    return new Pair<>(left, right);
  }

  /**
   * @return Left part of the Pair
   */
  public L getLeft()
  {
    return left;
  }

  /**
   * @return Right part of the Pair
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
    return (left == null ? 0 : left.hashCode()) ^ (right == null ? 0 : right.hashCode());
  }

  /**
   * Compares if the passed object is equal to the current Pair
   *
   * @param obj Object to compare
   *
   * @return True if equal or false if not
   */
  @Override
  public boolean equals(Object obj)
  {
    return (obj instanceof Pair) && equals((Pair) obj);
  }

  /**
   * @return String representation of this Pair
   */
  @Override
  public String toString()
  {
    return "(" + left + "," + right + ")";
  }

  /**
   * Compares if the passed Pair is equal to the current Pair
   * <p>
   * <p>
   * Two pairs are only equal if both the left and right of
   * the current pair are equal to the left and right of the
   * Pair being compared.
   * </p>
   *
   * @param pair Pair to compare
   *
   * @return True if equal or false if not
   */
  public boolean equals(Pair pair)
  {
    return pair != null && Objects.equals(this.left, pair.left) && Objects
      .equals(this.right, pair.right);
  }
}
