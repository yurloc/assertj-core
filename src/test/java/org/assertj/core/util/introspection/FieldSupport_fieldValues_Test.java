/*
 * Created on Feb 22, 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * Copyright @2011 the original author or authors.
 */
package org.assertj.core.util.introspection;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.test.ExpectedException.none;
import static org.assertj.core.util.Lists.newArrayList;

import java.util.List;

import org.assertj.core.test.Employee;
import org.assertj.core.test.ExpectedException;
import org.assertj.core.test.Name;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for <code>{@link FieldSupport#fieldValues(String, Class, Iterable)}</code>.
 * 
 * @author Joel Costigliola
 */
public class FieldSupport_fieldValues_Test {

  private Employee yoda;
  private Employee luke;
  private List<Employee> employees;

  @Before
  public void setUpOnce() {
    yoda = new Employee(1L, new Name("Yoda"), 800);
    luke = new Employee(2L, new Name("Luke", "Skywalker"), 26);
    employees = newArrayList(yoda, luke);
  }

  @Rule
  public ExpectedException thrown = none();

  @Test
  public void should_return_empty_List_if_given_Iterable_is_null() {
    Iterable<Long> ids = FieldSupport.instance().fieldValues("ids", Long.class, (Iterable<Long>) null);
    assertEquals(emptyList(), ids);
  }

  @Test
  public void should_return_empty_List_if_given_Iterable_is_empty() {
    Iterable<Long> ids = FieldSupport.instance().fieldValues("ids", Long.class, emptySet());
    assertEquals(emptyList(), ids);
  }

  @Test
  public void should_return_null_elements_for_null_field_value() {
    List<Employee> list = newArrayList(null, null);
    Iterable<Long> ages = FieldSupport.instance().fieldValues("id", Long.class, list);
    assertThat(ages).containsExactly(null, null);

    list = newArrayList(yoda, luke, null, null);
    ages = FieldSupport.instance().fieldValues("id", Long.class, list);
    assertThat(ages).containsExactly(1L, 2L, null, null);
  }

  @Test
  public void should_return_values_of_simple_field() {
    Iterable<Long> ids = FieldSupport.instance().fieldValues("id", Long.class, employees);
    assertEquals(newArrayList(1L, 2L), ids);
  }

  @Test
  public void should_return_values_of_nested_field() {
    Iterable<String> firstNames = FieldSupport.instance().fieldValues("name.first", String.class, employees);
    assertEquals(newArrayList("Yoda", "Luke"), firstNames);
  }

  @Test
  public void should_throw_error_if_field_not_found() {
    thrown.expect(IntrospectionError.class,
                  "Unable to obtain the value of the field <'id.'> from <Employee[id=1, name=Name[first='Yoda', last='null'], age=800]>");
    FieldSupport.instance().fieldValues("id.", Long.class, employees);
  }

  @Test
  public void should_return_values_of_private_field() {
    List<Integer> ages = FieldSupport.instance().fieldValues("age", Integer.class, employees);
    assertEquals(newArrayList(800, 26), ages);
  }

  @Test
  public void should_throw_error_if_field_not_public_and_allowExtractingPrivateFields_set_to_false() {
    FieldSupport.setAllowExtractingPrivateFields(false);
    try {
      thrown.expect(IntrospectionError.class,
                    "Unable to obtain the value of the field <'age'> from <Employee[id=1, name=Name[first='Yoda', last='null'], age=800]>, check that field is public.");
      FieldSupport.instance().fieldValues("age", Integer.class, employees);
    } finally { // back to default value
      FieldSupport.setAllowExtractingPrivateFields(true);
    }
  }

  @Test
  public void should_extract_field() {
    Long id = FieldSupport.instance().fieldValue("id", Long.class, yoda);
    assertEquals(Long.valueOf(1L), id);
  }

  @Test
  public void should_extract_nested_field() {
    String firstName = FieldSupport.instance().fieldValue("name.first", String.class, yoda);
    assertThat(firstName).isEqualTo("Yoda");
    
    yoda.name.first = null;
    firstName = FieldSupport.instance().fieldValue("name.first", String.class, yoda);
    assertThat(firstName).isNull();
    
    yoda.name = null;
    firstName = FieldSupport.instance().fieldValue("name.first", String.class, yoda);
    assertThat(firstName).isNull();
  }
  
  @Test
  public void should_handle_array_as_iterable() {
    List<Long> fieldValuesFromIterable = FieldSupport.instance().fieldValues("id", Long.class, employees);
    List<Long> fieldValuesFromArray = FieldSupport.instance().fieldValues("id", Long.class,
                                                                          employees.toArray(new Employee[0]));
    assertThat(fieldValuesFromArray).isEqualTo(fieldValuesFromIterable);
  }
}
