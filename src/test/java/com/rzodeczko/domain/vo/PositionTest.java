package com.rzodeczko.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Position")
class PositionTest {

    @Nested
    @DisplayName("Constructor (Integer, Integer)")
    class ConstructorTests {

        @Test
        @DisplayName("Valid integers: creates Position")
        void shouldCreatePositionFromIntegers() {
            Position position = new Position(5, 10);

            assertThat(position.rowNo()).isEqualTo(5);
            assertThat(position.colNo()).isEqualTo(10);
        }

        @Test
        @DisplayName("Zero values: creates Position")
        void shouldCreatePositionWithZeros() {
            Position position = new Position(0, 0);

            assertThat(position.rowNo()).isEqualTo(0);
            assertThat(position.colNo()).isEqualTo(0);
        }

        @Test
        @DisplayName("Null values: creates Position with nulls")
        void shouldCreatePositionWithNulls() {
            Position position = new Position(null, null);

            assertThat(position.rowNo()).isNull();
            assertThat(position.colNo()).isNull();
        }

        @Test
        @DisplayName("Negative integers: creates Position")
        void shouldCreatePositionWithNegatives() {
            Position position = new Position(-1, -2);

            assertThat(position.rowNo()).isEqualTo(-1);
            assertThat(position.colNo()).isEqualTo(-2);
        }
    }

    @Nested
    @DisplayName("Constructor (String)")
    class ConstructorStringTests {

        @Test
        @DisplayName("Valid format: parses and creates Position")
        void shouldCreatePositionFromValidString() {
            Position position = new Position("rowNo: 5, colNo: 10");

            assertThat(position.rowNo()).isEqualTo(5);
            assertThat(position.colNo()).isEqualTo(10);
        }

        @Test
        @DisplayName("Single digit values: parses correctly")
        void shouldParseStringWithSingleDigits() {
            Position position = new Position("rowNo: 1, colNo: 2");

            assertThat(position.rowNo()).isEqualTo(1);
            assertThat(position.colNo()).isEqualTo(2);
        }

        @Test
        @DisplayName("Large values: parses correctly")
        void shouldParseStringWithLargeValues() {
            Position position = new Position("rowNo: 999, colNo: 888");

            assertThat(position.rowNo()).isEqualTo(999);
            assertThat(position.colNo()).isEqualTo(888);
        }

        @Test
        @DisplayName("Invalid format (missing comma): results in nulls")
        void shouldResultInNullsForInvalidFormat() {
            Position position = new Position("rowNo: 5 colNo: 10");

            assertThat(position.rowNo()).isNull();
            assertThat(position.colNo()).isNull();
        }

        @Test
        @DisplayName("Malformed string: returns nulls")
        void shouldHandleMalformedString() {
            Position position = new Position("invalid");

            assertThat(position.rowNo()).isNull();
            assertThat(position.colNo()).isNull();
        }
    }

    @Nested
    @DisplayName("No-arg constructor")
    class NoArgConstructorTests {

        @Test
        @DisplayName("Creates Position with null values")
        void shouldCreatePositionWithNulls() {
            Position position = new Position();

            assertThat(position.rowNo()).isNull();
            assertThat(position.colNo()).isNull();
        }
    }

    @Nested
    @DisplayName("setRowNo(Integer)")
    class SetRowNoTests {

        @Test
        @DisplayName("Valid row: creates new Position with new row")
        void shouldSetNewRowNo() {
            Position position = new Position(5, 10);

            Position updated = position.withRowNo(15);

            assertThat(updated.rowNo()).isEqualTo(15);
            assertThat(updated.colNo()).isEqualTo(10);
            assertThat(position.rowNo()).isEqualTo(5); // Original unchanged
        }

        @Test
        @DisplayName("Null row: creates new Position with null row")
        void shouldSetRowNoToNull() {
            Position position = new Position(5, 10);

            Position updated = position.withRowNo(null);

            assertThat(updated.rowNo()).isNull();
            assertThat(updated.colNo()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("setColNo(Integer)")
    class SetColNoTests {

        @Test
        @DisplayName("Valid column: creates new Position with new column")
        void shouldSetNewColNo() {
            Position position = new Position(5, 10);

            Position updated = position.withColNo(20);

            assertThat(updated.rowNo()).isEqualTo(5);
            assertThat(updated.colNo()).isEqualTo(20);
            assertThat(position.colNo()).isEqualTo(10); // Original unchanged
        }

        @Test
        @DisplayName("Null column: creates new Position with null column")
        void shouldSetColNoToNull() {
            Position position = new Position(5, 10);

            Position updated = position.withColNo(null);

            assertThat(updated.rowNo()).isEqualTo(5);
            assertThat(updated.colNo()).isNull();
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("Valid position: returns formatted string")
        void shouldReturnFormattedString() {
            Position position = new Position(5, 10);

            String result = position.toString();

            assertThat(result).isEqualTo("rowNo: 5, colNo: 10");
        }

        @Test
        @DisplayName("Null values: includes nulls in string")
        void shouldHandleNullsInString() {
            Position position = new Position(null, null);

            String result = position.toString();

            assertThat(result).isEqualTo("rowNo: null, colNo: null");
        }

        @Test
        @DisplayName("Mixed values: includes both in string")
        void shouldHandleMixedValuesInString() {
            Position position = new Position(5, null);

            String result = position.toString();

            assertThat(result).isEqualTo("rowNo: 5, colNo: null");
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Build with both values: creates Position")
        void shouldBuildPositionWithBothValues() {
            Position position = Position.builder()
                    .rowNo(7)
                    .colNo(14)
                    .build();

            assertThat(position.rowNo()).isEqualTo(7);
            assertThat(position.colNo()).isEqualTo(14);
        }

        @Test
        @DisplayName("Build with only rowNo: creates Position with null colNo")
        void shouldBuildPositionWithOnlyRowNo() {
            Position position = Position.builder()
                    .rowNo(3)
                    .build();

            assertThat(position.rowNo()).isEqualTo(3);
            assertThat(position.colNo()).isNull();
        }

        @Test
        @DisplayName("Build with only colNo: creates Position with null rowNo")
        void shouldBuildPositionWithOnlyColNo() {
            Position position = Position.builder()
                    .colNo(8)
                    .build();

            assertThat(position.rowNo()).isNull();
            assertThat(position.colNo()).isEqualTo(8);
        }

        @Test
        @DisplayName("Build without any values: creates Position with nulls")
        void shouldBuildPositionWithNulls() {
            Position position = Position.builder()
                    .build();

            assertThat(position.rowNo()).isNull();
            assertThat(position.colNo()).isNull();
        }

        @Test
        @DisplayName("Builder is reusable: allows multiple builds")
        void shouldAllowChaining() {
            var builder = Position.builder()
                    .rowNo(5)
                    .colNo(10);

            Position position1 = builder.build();
            Position position2 = builder
                    .rowNo(3)
                    .build();

            assertThat(position1.rowNo()).isEqualTo(5);
            assertThat(position1.colNo()).isEqualTo(10);

            assertThat(position2.rowNo()).isEqualTo(3);
            assertThat(position2.colNo()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Serialization")
    class SerializationTests {

        @Test
        @DisplayName("Position implements Serializable")
        void shouldBeSerializable() {
            Position position = new Position(5, 10);

            assertThat(position).isInstanceOf(java.io.Serializable.class);
        }
    }

    @Nested
    @DisplayName("Equality and Hashing")
    class EqualityTests {

        @Test
        @DisplayName("Same values: are equal (record equality)")
        void shouldBeEqualWithSameValues() {
            Position position1 = new Position(5, 10);
            Position position2 = new Position(5, 10);

            assertThat(position1).isEqualTo(position2);
        }

        @Test
        @DisplayName("Different values: are not equal")
        void shouldNotBeEqualWithDifferentValues() {
            Position position1 = new Position(5, 10);
            Position position2 = new Position(5, 11);

            assertThat(position1).isNotEqualTo(position2);
        }

        @Test
        @DisplayName("Same hash code: for equal positions")
        void shouldHaveSameHashCodeWhenEqual() {
            Position position1 = new Position(5, 10);
            Position position2 = new Position(5, 10);

            assertThat(position1.hashCode()).isEqualTo(position2.hashCode());
        }
    }
}

