/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.jpa.test.query;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.util.Date;
import java.util.List;

import org.hibernate.jpa.test.BaseEntityManagerFunctionalTestCase;

import org.junit.Test;

import org.hibernate.testing.junit4.BaseUnitTestCase;

import static org.hibernate.testing.junit4.ExtraAssertions.assertTyping;
import static org.junit.Assert.assertEquals;

/**
 * @author Steve Ebersole
 */
public class ConstructorResultNativeQueryTest extends BaseEntityManagerFunctionalTestCase {
	@Entity( name = "Person" )
	@SqlResultSetMapping(
			name = "person-id-and-name",
			classes = {
					@ConstructorResult(
							targetClass = Person.class,
							columns = {
									@ColumnResult( name = "id" ),
									@ColumnResult( name = "p_name" )
							}
					)
			}
	)
	@NamedNativeQuery(
			name = "person-id-and-name",
			query = "select p.id, p.p_name from person p order by p.p_name",
			resultSetMapping = "person-id-and-name"
	)
	public static class Person {
		@Id
		private Integer id;
		@Column( name = "p_name" )
		private String name;
		@Temporal( TemporalType.TIMESTAMP )
		private Date birthDate;

		public Person() {
		}

		public Person(Integer id, String name, Date birthDate) {
			this.id = id;
			this.name = name;
			this.birthDate = birthDate;
		}

		public Person(Integer id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] { Person.class };
	}


	@Test
	public void testConstructorResultNativeQuery() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();
		em.persist( new Person( 1, "John", new Date() ) );
		em.getTransaction().commit();
		em.close();

		em = getOrCreateEntityManager();
		em.getTransaction().begin();
		List results = em.createNativeQuery(
				"select p.id, p.p_name from person p order by p.p_name",
				"person-id-and-name"
		).getResultList();
		assertEquals( 1, results.size() );
		assertTyping( Person.class, results.get( 0 ) );
		em.getTransaction().commit();
		em.close();

		em = getOrCreateEntityManager();
		em.getTransaction().begin();
		em.createQuery( "delete from Person" ).executeUpdate();
		em.getTransaction().commit();
		em.close();
	}
}
