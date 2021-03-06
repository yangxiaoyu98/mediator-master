package org.fmgroup.mediator.language.term;

import org.antlr.v4.runtime.ParserRuleContext;
import org.fmgroup.mediator.language.RawElement;
import org.fmgroup.mediator.language.ValidationException;
import org.fmgroup.mediator.language.generated.MediatorLangParser;
import org.fmgroup.mediator.language.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TupleTerm implements Term {

    private List<Term> values = new ArrayList<>();
    private RawElement parent = null;

    public List<Term> getValues() {
        return values;
    }

    public TupleTerm setValues(List<Term> values) {
        this.values = new ArrayList<>();
        values.forEach(this::addValue);
        return this;
    }

    public TupleTerm addValue(Term value) {
        values.add(value);
        value.setParent(this);
        return this;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public int getPrecedence() {
        return 13;
    }

    @Override
    public TupleTerm fromContext(ParserRuleContext context, RawElement parent) throws ValidationException {
        if (!(context instanceof MediatorLangParser.TupleTermContext)) {
            throw ValidationException.IncompatibleContextType(this.getClass(), "TupleTermContext", context.toString())
                    .At(context);
        }

        setParent(parent);

        for (MediatorLangParser.TermContext t: ((MediatorLangParser.TupleTermContext) context).term()) {
            addValue(
                    Term.parse(t, this)
            );
        }

        return this;
    }

    @Override
    public String toString() {
        return
                "(" +
                        this.values.stream().map(Object::toString).collect(Collectors.joining(", "))
                        + ")";
    }

    @Override
    public RawElement getParent() {
        return this.parent;
    }

    @Override
    public RawElement setParent(RawElement parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public TupleTerm copy(RawElement parent) throws ValidationException {
        TupleTerm nlt = (TupleTerm) new TupleTerm().setParent(parent);
        for (Term t : getValues()) {
            nlt.addValue(t.copy(nlt));
        }
        return nlt;
    }

    @Override
    public Term refactor(Map<String, Type> typeRewriteMap, Map<String, Term> termRewriteMap) throws ValidationException {
        List<Term> lstterms = new ArrayList<>();
        for (Term t : getValues()) {
            lstterms.add(t.refactor(typeRewriteMap, termRewriteMap));
        }
        setValues(lstterms);

        return this;
    }
}
