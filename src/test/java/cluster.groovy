/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package examples

// Define the cluster
b = new oms3.SimBuilder(logging:'CONFIG');
c = b.cluster(name: 'all') {
    // all the boxes that
//    ec2(keys:'C:/Documents and Settings/od/.awssecret', ami_id:'ami-21a74048')
    node '1.1.1.1'
    node '123.3.4.[1-5]'
//    cluster(name:'ec2') {
//        node '123.3.4.10'
//    }
//    cluster(name:'euc') {
//        node '123.3.4.35'
//        node '232.3.100.6'
//    }
}
//println c.nodes()

System.out.println(c.nodes())
//c.execute("ls -l");
//c.execute("ls -l");
//c.put("a.txt", "/tmp");







