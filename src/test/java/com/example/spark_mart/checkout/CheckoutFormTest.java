package com.example.spark_mart.checkout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckoutFormTest {

    private CheckoutForm form;

    @BeforeEach
    void setUp() {
        form = new CheckoutForm();
        form.setName("John Doe");
        form.setEmail("john@example.com");
        form.setPhone("01700000000");
        form.setAddress("123 Main St");
        form.setArea("Dhaka");
        form.setConsent(true);
    }

    @Test
    void completeFormPassesValidation() {
        assertTrue(form.isComplete());
    }

    @Test
    void missingNameFailsValidation() {
        form.setName(null);
        assertFalse(form.isComplete());
    }

    @Test
    void blankNameFailsValidation() {
        form.setName("   ");
        assertFalse(form.isComplete());
    }

    @Test
    void missingEmailFailsValidation() {
        form.setEmail(null);
        assertFalse(form.isComplete());
    }

    @Test
    void missingPhoneFailsValidation() {
        form.setPhone(null);
        assertFalse(form.isComplete());
    }

    @Test
    void missingAddressFailsValidation() {
        form.setAddress(null);
        assertFalse(form.isComplete());
    }

    @Test
    void missingConsentFailsValidation() {
        form.setConsent(false);
        assertFalse(form.isComplete());
    }

    @Test
    void areaAndNoteAreOptional() {
        form.setArea(null);
        form.setNote(null);
        assertTrue(form.isComplete());
    }

    @Test
    void defaultPaymentMethodIsCash() {
        CheckoutForm freshForm = new CheckoutForm();
        assertEquals("cash", freshForm.getPaymentMethod());
    }

    private void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
