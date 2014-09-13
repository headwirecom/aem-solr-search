package com.headwire.aemsolrsearch.taglib;

import java.util.Collection;

import com.squeakysand.jsp.tagext.annotations.JspElFunction;

public final class CommonElFunctions {

	@JspElFunction
	public static int round(double v) {
		return Long.valueOf(Math.round(v)).intValue();
	}
	
	@JspElFunction
	public static int floor(double v) {
		return Double.valueOf(Math.floor(v)).intValue();
	}

	@JspElFunction
	public static int ceil(double v) {
		return Double.valueOf(Math.ceil(v)).intValue();
	}

	@JspElFunction
	public static int min(int a, int b) {
		return Math.min(a, b);
	}

	@JspElFunction
	public static int max(int a, int b) {
		return Math.max(a, b);
	}

	@JspElFunction
	// Note that overloading is not really supported in JSTL El Functions, so
	// this is a work around.
	public static boolean contains(Object objectThatMayContains, Object obj) {
		if (objectThatMayContains instanceof String
				&& obj instanceof CharSequence) {
			return contains((String) objectThatMayContains, (CharSequence) obj);
		} else if (objectThatMayContains instanceof Collection) {
			return contains((Collection<?>) objectThatMayContains, obj);
		} else if (objectThatMayContains instanceof Object[]) {
			return contains((Object[]) objectThatMayContains, obj);
		} else {
			return false;
		}
	}

	private static boolean contains(String s, CharSequence substring) {
		return s != null && s.contains(substring);
	}

	private static boolean contains(Collection<?> collection, Object obj) {
		return collection != null && collection.contains(obj);
	}

	private static boolean contains(Object[] array, Object obj) {
		if (null != array) {
			for (Object element : array) {
				if (obj == element) {
					return true;
				} else if ((obj != null && element != null)
						&& (obj.hashCode() == element.hashCode())
						&& (obj.equals(element))) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

}
