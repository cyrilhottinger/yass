package ch.softappeal.yass.tutorial.client;

import ch.softappeal.yass.ts.ContractGenerator;
import ch.softappeal.yass.tutorial.contract.Config;

import java.io.PrintWriter;

public final class GenerateContract {

    public static void main(final String... args) throws Exception {
        new ContractGenerator(
            Config.class.getPackage(), Config.CONTRACT_SERIALIZER, Config.METHOD_MAPPER_FACTORY,
            "baseTypes", "contract", "src/tutorial/ts/contract.ts"
        );
        Config.CONTRACT_SERIALIZER.print(new PrintWriter(System.out, true));
    }

}
